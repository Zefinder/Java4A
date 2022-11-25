package clavardaj.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
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

	private DatagramSocket server;
	public static final BlockingQueue<PacketToReceive> packetsToHandle = new LinkedBlockingQueue<>();

	private final int UDP_PORT = 1233;
	private final int TCP_PORT = 1234;

	private int nextAvailablePort;

	private PacketManager() {
		nextAvailablePort = TCP_PORT;
		distantSockets = new ArrayList<>();
		idToPacket.put(0, PacketRcvLogin.class);

		packetToId.put(PacketEmtLogin.class, 0);

	}

	@Override
	public void run() {
		ServerSocket server;
		ServerSocket newServer;
		try {
			// Serveur global de redirection TCP
			server = new ServerSocket(nextAvailablePort++);

			while (true) {
				Socket socket = server.accept();
				new DataOutputStream(socket.getOutputStream()).writeInt(nextAvailablePort++);
				socket.close();

				newServer = new ServerSocket(nextAvailablePort);
				socket = newServer.accept();
				distantSockets.add(socket);
				new DataOutputStream(socket.getOutputStream()).writeUTF("Salut mec");
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
					packet = new DatagramPacket(buf, buf.length, address, port);

					String sDistantPort = new String(packet.getData(), 0, packet.getLength()).trim();

					int distantPort;
					try {
						distantPort = Integer.valueOf(sDistantPort);
					} catch (NumberFormatException e) {
						System.err.println("Packet received not for our application!");
						continue;
					}

					if (Main.DEBUG)
						System.out.println("Nouvelle connexion initiée au port " + distantPort);

					// On ouvre une connexion TCP entre les deux PacketManager. On se fera
					// rediriger...
					try {
						Socket client = new Socket(address, distantPort);
						DataInputStream in = new DataInputStream(client.getInputStream());

						int newPort = in.readInt();

						if (Main.DEBUG)
							System.out.println("[Server]: Redirection sur le port " + newPort);

						client.close();
						client = new Socket(address, newPort);

						in = new DataInputStream(client.getInputStream());
						// On lance l'écoute de paquets pour TCP
						new Thread(new PacketThread(in)).start();
						distantSockets.add(client);

						if (Main.DEBUG)
							System.out.println("[Server]: Message Reçu " + in.readUTF());

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		// On dit à tout le monde qu'on est prêts !
		broadcastLogin();
	}

	private void broadcastLogin() {
		// On envoie qu'on est connecté sur le port UDP (port UDP_PORT pour tout le
		// monde)
		// On dit que notre redirection TCP est sur TCP_PORT

	}

	public void sendPacket(DataOutputStream outputStream, PacketToEmit packet) {
		try {
			outputStream.writeInt(packetToId.get(packet.getClass()));
			packet.sendPacket(outputStream);

			if (Main.DEBUG)
				System.out.println("Packet sent: " + packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static PacketManager getInstance() {
		return instance;
	}

	public static void main(String[] args) throws IOException {
//		byte[] buf;
//		PacketManager.getInstance().setPort(1234);
//
//		ServerSocket serverSocket = new ServerSocket(1236);
//
//		buf = "1236".getBytes();
//		DatagramSocket socket = new DatagramSocket();
//		DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), 1234);
//		socket.send(packet);
//		socket.close();
//
//		Socket s = serverSocket.accept();
//		DataOutputStream out = new DataOutputStream(s.getOutputStream());
//		DataInputStream in = new DataInputStream(s.getInputStream());
//
//		System.out.println("[Client]: Message reçu : " + in.readUTF());
//		out.writeUTF("Salut !");

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
				System.out.println("Packet fully read: " + packet);

			return packet;
		}

		@Override
		public void run() {
			while (true)
				try {
					int idPacket = inputStream.readInt();
					if (Main.DEBUG)
						System.out.println("Packet " + idPacket + " received !");

					PacketToReceive packet = readPacket(idPacket);
					packetsToHandle.add(packet);
				} catch (IOException | NoSuchMethodException | InvocationTargetException | InstantiationException
						| IllegalAccessException e) {
					e.printStackTrace();
					break;
				}
		}
	}

}
