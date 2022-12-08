package clavardaj.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import clavardaj.Main;
import clavardaj.controller.listener.LoginListener;
import clavardaj.model.Agent;
import clavardaj.model.packet.emit.PacketEmtCloseConversation;
import clavardaj.model.packet.emit.PacketEmtLogin;
import clavardaj.model.packet.emit.PacketEmtLoginChange;
import clavardaj.model.packet.emit.PacketEmtLogout;
import clavardaj.model.packet.emit.PacketEmtMessage;
import clavardaj.model.packet.emit.PacketEmtOpenConversation;
import clavardaj.model.packet.emit.PacketToEmit;
import clavardaj.model.packet.receive.PacketRcvCloseConversation;
import clavardaj.model.packet.receive.PacketRcvLogin;
import clavardaj.model.packet.receive.PacketRcvLoginChange;
import clavardaj.model.packet.receive.PacketRcvLogout;
import clavardaj.model.packet.receive.PacketRcvMessage;
import clavardaj.model.packet.receive.PacketRcvOpenConversation;
import clavardaj.model.packet.receive.PacketToReceive;

/**
 * 
 * Deux scénarios :<br/>
 * <ul>
 * <li>On se login et on envoie un paquet UDP avec le numéro de port de la porte
 * TCP. On reçoit une réponse TCP et on redirige vers un nouveau port disponible
 * ! On enregistre ensuite la socket et on lance l'écoute de packet
 * 
 * <li>On était déjà login et on reçoit un paquet UDP. On crée donc une
 * connection vers la porte TCP (port contenu dans le message). On reçoit
 * ensuite un numéro de port libre pour la connection TCP continue
 * </ul>
 * 
 * @author Adrien Jakubiak
 *
 */
public class PacketManager implements Runnable, LoginListener {

	private static final PacketManager instance = new PacketManager();

	private final Map<Integer, Class<? extends PacketToReceive>> idToPacket = new HashMap<>();
	private final Map<Class<? extends PacketToEmit>, Integer> packetToId = new HashMap<>();

	// TODO : mettre le socket du packet manager principal en attribut pour faire onSelfLogout
	// Changements répercutés dans run
	private List<ServerSocket> distantServerSockets;
	private List<Socket> distantSockets;
	private List<InetAddress> localAddresses;

	private DatagramSocket server;
	public static final BlockingQueue<PacketToReceive> packetsToHandle = new LinkedBlockingQueue<>();

	private final int UDP_PORT = 1233;
	private final int TCP_PORT = 1234;

	private int nextAvailablePort;

	private PacketManager() {
		nextAvailablePort = TCP_PORT;
		distantServerSockets = new ArrayList<>();
		distantSockets = new ArrayList<>();
		localAddresses = new ArrayList<>();

		// On récupère toutes les adresses correspondantes à cette machine
		try {
			Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaceEnumeration.hasMoreElements()) {
				for (InterfaceAddress interfaceAddress : networkInterfaceEnumeration.nextElement()
						.getInterfaceAddresses())
					if (interfaceAddress.getAddress().isSiteLocalAddress())
						localAddresses.add(interfaceAddress.getAddress());
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}

		idToPacket.put(0, PacketRcvLogin.class);
		idToPacket.put(1, PacketRcvLogout.class);
		idToPacket.put(2, PacketRcvOpenConversation.class);
		idToPacket.put(3, PacketRcvCloseConversation.class);
		idToPacket.put(4, PacketRcvMessage.class);
		idToPacket.put(5, PacketRcvLoginChange.class);

		packetToId.put(PacketEmtLogin.class, 0);
		packetToId.put(PacketEmtLogout.class, 1);
		packetToId.put(PacketEmtOpenConversation.class, 2);
		packetToId.put(PacketEmtCloseConversation.class, 3);
		packetToId.put(PacketEmtMessage.class, 4);
		packetToId.put(PacketEmtLoginChange.class, 5);

		new Thread(this).start();
	}

	@Override
	public void run() {
		ServerSocket server;
		ServerSocket newServer;
		try {
			// Serveur global de redirection TCP

			if (Main.DEBUG)
				System.out.println("[Server]: TCP door opened!");
			server = new ServerSocket(nextAvailablePort++);

			while (true) {
				Socket socket = server.accept();

				if (Main.DEBUG)
					System.out.println("[Server]: Client connected... Redirecting to port " + nextAvailablePort + "!");

				new DataOutputStream(socket.getOutputStream()).writeInt(nextAvailablePort);
				socket.close();

				newServer = new ServerSocket(nextAvailablePort++);
				socket = newServer.accept();
				
				if (Main.DEBUG)
					System.out.println("[Server]: Client redirected, ready to transfer packets!");

				DataInputStream in = new DataInputStream(socket.getInputStream());
				// On lance l'écoute de paquets pour TCP
				new Thread(new PacketThread(in)).start();
				distantServerSockets.add(newServer);
				distantSockets.add(socket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void init() {
		try {
			server = new DatagramSocket(UDP_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// On dit à tout le monde qu'on est prêts !
		try {
			broadcastLogin();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Lancer le thread d'attente de connexion
		new Thread(new Runnable() {
			private byte[] buf = new byte[5];

			@Override
			public void run() {
				// TODO: Vérifier que l'UDP de connexion ne vient pas d'une connexion déjà
				// présente

				while (true) {
					// On prend le paquet UDP
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					try {
						server.receive(packet);
					} catch (IOException e) {
						e.printStackTrace();
					}

					// On dissèque le paquet
					InetAddress address = packet.getAddress();
					int port = packet.getPort();

					if (Main.DEBUG)
						System.out.println("[Server]: UDP Packet received from address " + address.getHostAddress());

					// On regarde si le paquet ne vient pas de nous...
					boolean local = false;
					for (InetAddress localAddress : localAddresses)
						if (address.equals(localAddress)) {
							local = true;
							if (Main.DEBUG)
								System.err.println("[Server]: UDP Packet was from us!");
						}

					if (local)
						continue;

					// Sinon on continue
					packet = new DatagramPacket(buf, buf.length, address, port);

					String sDistantPort = new String(packet.getData(), 0, packet.getLength()).trim();

					int distantPort;
					try {
						distantPort = Integer.valueOf(sDistantPort);
					} catch (NumberFormatException e) {
						System.err.println("[Server]: Packet received not for our application!");
						continue;
					}

					if (Main.DEBUG)
						System.out.println("[Server]: New connection detected... TCP connection to port " + distantPort
								+ " on distant host...");

					// On ouvre une connexion TCP entre les deux PacketManager. On se fera
					// rediriger...
					try {
						Socket client = new Socket(address, distantPort);
						DataInputStream in = new DataInputStream(client.getInputStream());

						int newPort = in.readInt();

						if (Main.DEBUG) {
							System.out.println("[Server]: Connected to TCP door...");
							System.out.println("[Server]: Redirected to port " + newPort + " on distant host!");
						}
						client.close();
						client = new Socket(address, newPort);

						in = new DataInputStream(client.getInputStream());
						// On lance l'écoute de paquets pour TCP
						new Thread(new PacketThread(in)).start();
						distantSockets.add(client);

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private void broadcastLogin() throws IOException {
		// On envoie qu'on est connecté sur le port UDP (port UDP_PORT pour tout le
		// monde)
		// On dit que notre redirection TCP est sur TCP_PORT

		if (Main.DEBUG)
			System.out.println("[Server]: Broadcasting packet for connexion...");

		byte[] buf;
		buf = "1234".getBytes();

		DatagramSocket socket = new DatagramSocket();
		socket.setBroadcast(true);
		// TODO: Trouver un broadcast valide partout !
		DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("192.168.43.97"), UDP_PORT);
		socket.send(packet);
		socket.close();
	}

	public void sendPacket(DataOutputStream outputStream, PacketToEmit packet) {
		try {
			outputStream.writeInt(packetToId.get(packet.getClass()));
			packet.sendPacket(outputStream);

			if (Main.DEBUG)
				System.out.println("[Server]: Packet sent: " + packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static PacketManager getInstance() {
		return instance;
	}

	public DataOutputStream getLocalPipe() {
		return null;
	}

	public static void main(String[] args) throws IOException {
		instance.init();
//		System.out.println(InetAddress.getLocalHost().getHostAddress());
	}

	@Override
	public void onAgentLogin(Agent agent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onAgentLogout(Agent agent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSelfLogin(UUID uuid, String name) {
		init();
	}

	@Override
	public void onSelfLogout() {
		distantSockets.forEach(socket -> {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		distantServerSockets.forEach(socket -> {
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
	}

	private class PacketThread implements Runnable {

		private DataInputStream inputStream;

		public PacketThread(DataInputStream inputStream) {
			this.inputStream = inputStream;
		}

		private PacketToReceive readPacket(int idPacket)
				throws InstantiationException, IllegalAccessException, IllegalArgumentException,
				InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
			Class<? extends PacketToReceive> clazz = idToPacket.get(idPacket);
			PacketToReceive packet = clazz.getDeclaredConstructor().newInstance();

			packet.initFromStream(inputStream);

			if (Main.DEBUG)
				System.out.println("[Server]: Packet fully read: " + packet);

			return packet;
		}

		@Override
		public void run() {
			while (true)
				try {
					int idPacket = inputStream.readInt();
					if (Main.DEBUG)
						System.out.println("[Server]: Packet " + idPacket + " received !");

					PacketToReceive packet = readPacket(idPacket);
					packet.processPacket();
//					packetsToHandle.add(packet);
				} catch (IOException | InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
					break;
				}
		}
	}

}
