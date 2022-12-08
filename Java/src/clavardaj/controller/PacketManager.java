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
public class PacketManager implements LoginListener {

	private static final PacketManager instance = new PacketManager();

	private final Map<Integer, Class<? extends PacketToReceive>> idToPacket;
	private final Map<Class<? extends PacketToEmit>, Integer> packetToId;
	private final Map<InetAddress, Socket> ipToSocket;

	private List<InetAddress> localAddresses;

	private DatagramSocket server;
	public static final BlockingQueue<PacketToReceive> packetsToHandle = new LinkedBlockingQueue<>();

	private final int UDP_PORT = 1233;
	private final int TCP_PORT = 1234;

	private int nextAvailablePort;

	private Socket activeSocket;

	private PacketManager() {
		nextAvailablePort = TCP_PORT;
		localAddresses = new ArrayList<>();

		idToPacket = new HashMap<>();
		packetToId = new HashMap<>();
		ipToSocket = new HashMap<>();

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

		// On lance le thread d'écoute TCP
		new Thread(new TCPServerThread()).start();

		// On lance le thread d'écoute UDP
		new Thread(new UDPServerThread()).start();
	}

	public void init() {
		// On dit à tout le monde qu'on est prêts !
		try {
			broadcastLogin();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void broadcastLogin() throws IOException {
		// On envoie qu'on est connecté sur le port UDP (port UDP_PORT pour tout le
		// monde)
		// On dit que notre redirection TCP est sur TCP_PORT

		if (Main.DEBUG)
			System.out.println("[Server]: Broadcasting packet for connexion...");

		byte[] buf;
		buf = String.format("%d", TCP_PORT).getBytes();

		DatagramSocket socket = new DatagramSocket();
		socket.setBroadcast(true);
		// TODO: Trouver un broadcast valide partout !
		DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("192.168.43.97"), UDP_PORT);
		socket.send(packet);
		socket.close();
	}

	public void sendPacket(InetAddress ip, PacketToEmit packet) {
		try {
			DataOutputStream outputStream = new DataOutputStream(ipToSocket.get(ip).getOutputStream());
			outputStream.writeInt(packetToId.get(packet.getClass()));
			packet.sendPacket(outputStream);

			if (Main.DEBUG)
				System.out.println("[Server]: Packet sent: " + packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getNextAvailablePort() {
		return nextAvailablePort++;
	}

	public static PacketManager getInstance() {
		return instance;
	}

	public DataOutputStream getLocalPipe() {
		return null;
	}

	@Override
	public void onAgentLogin(Agent agent) {
	}

	@Override
	public void onAgentLogout(Agent agent) {

		Socket socket = ipToSocket.get(agent.getIp());
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ipToSocket.remove(agent.getIp());
	}

	@Override
	public void onSelfLogin(UUID uuid, String name) {
		// init();
	}

	@Override
	public void onSelfLogout() {

		// préférable de le laisser dans un try/catch à part
		try {
			activeSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ipToSocket.forEach((t, u) -> {
			try {
				u.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private class TCPServerThread implements Runnable {

		@Override
		public void run() {
			ServerSocket server;
			try {
				// Serveur global de redirection TCP

				if (Main.DEBUG)
					System.out.println("[Server]: TCP door opened!");
				server = new ServerSocket(nextAvailablePort++);

				while (true) {
					activeSocket = server.accept();

					if (Main.DEBUG)
						System.out.println(
								"[Server]: Client connected... Redirecting to port " + nextAvailablePort + "!");

					new DataOutputStream(activeSocket.getOutputStream()).writeInt(nextAvailablePort);
					activeSocket.close();

					ServerSocket newServer = null;
					while (newServer == null)
						try {
							newServer = new ServerSocket(nextAvailablePort++);
						} catch (IOException e) {
							System.err.println("[PacketManager] Port already used for newServer");
						}

					Socket socket = newServer.accept();

					if (Main.DEBUG)
						System.out.println("[Server]: Client redirected, ready to transfer packets!");

					DataInputStream in = new DataInputStream(socket.getInputStream());

					// On lance l'écoute de paquets pour TCP
					new Thread(new PacketThread(in)).start();
					newServer.close();
					
					// On envoie un login packet à la machine distante avec notre nom !
					if (socket.getInetAddress().toString().equals("/127.0.0.1"))
						for (InetAddress ip : localAddresses)
							ipToSocket.put(ip, socket);

					ipToSocket.put(socket.getInetAddress(), socket);

					Agent agent = UserManager.getInstance().getCurrentAgent();
					sendPacket(socket.getInetAddress(), new PacketEmtLogin(agent.getUuid(), socket.getLocalAddress(), agent.getName()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private class UDPServerThread implements Runnable {
		private byte[] buf = new byte[5];

		@Override
		public void run() {
			// TODO: Vérifier que l'UDP de connexion ne vient pas d'une connexion déjà
			// présente

			try {
				server = new DatagramSocket(UDP_PORT);
			} catch (SocketException e1) {
				e1.printStackTrace();
			}

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

					ipToSocket.put(client.getInetAddress(), client);

					// On envoie un login packet à la machine distante avec notre nom !
					Agent agent = UserManager.getInstance().getCurrentAgent();
					sendPacket(client.getInetAddress(), new PacketEmtLogin(agent.getUuid(), agent.getIp(), agent.getName()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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

					PacketToReceive packet = readPacket(idPacket);
					packet.processPacket();
					// TODO Ne pas process packet tout de suite mais le faire dans un thread à part !
//					packetsToHandle.add(packet);
				} catch (IOException | InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {

					if (e instanceof IOException) {
						if (Main.DEBUG)
							System.out.println("[PacketThread] Socket closed");
						
//						ListenerManager.getInstance().fireAgentLogout();
					} else
						e.printStackTrace();

					break;

				}
		}
	}

}
