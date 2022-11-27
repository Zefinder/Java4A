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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import clavardaj.Main;
import clavardaj.model.packet.emit.PacketEmtLogin;
import clavardaj.model.packet.emit.PacketToEmit;
import clavardaj.model.packet.receive.PacketRcvLogin;
import clavardaj.model.packet.receive.PacketToReceive;

public class PacketManager implements Runnable {

	private static final PacketManager instance = new PacketManager();

	private final Map<Integer, Class<? extends PacketToReceive>> idToPacket = new HashMap<>();
	private final Map<Class<? extends PacketToEmit>, Integer> packetToId = new HashMap<>();

	private List<Socket> distantSockets;
	private List<InetAddress> localAddresses;

	private DatagramSocket server;
	public static final BlockingQueue<PacketToReceive> packetsToHandle = new LinkedBlockingQueue<>();

	private final int UDP_PORT = 1233;
	private final int TCP_PORT = 1234;

	private int nextAvailablePort;

	private PacketManager() {
		nextAvailablePort = TCP_PORT;
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

		packetToId.put(PacketEmtLogin.class, 0);

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

				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				sendPacket(out, new PacketEmtLogin("Bébou"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void setPort() {
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

	public static void main(String[] args) throws IOException {
		instance.setPort();
//		System.out.println(InetAddress.getLocalHost().getHostAddress());
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
