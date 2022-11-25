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
import java.util.HashMap;
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
	private DataInputStream inputStream;
	private DataOutputStream outputStream;

	private DatagramSocket server;
	public static final BlockingQueue<PacketToReceive> packetsToHandle = new LinkedBlockingQueue<>();

	private int nextAvailablePort;

	private PacketManager() {
		nextAvailablePort = 1234;
		idToPacket.put(0, PacketRcvLogin.class);

		packetToId.put(PacketEmtLogin.class, 0);

	}

	public void connect() {
		// TODO Send UDP Packet to send communication ports !

	}

	public void setPort(int port) {
		try {
			server = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}

		// Lancer le thread d'attente de connexion
		new Thread(new Runnable() {

			private byte[] buf = new byte[5];

			@Override
			public void run() {
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
					int distantPort = Integer.valueOf(sDistantPort);

					if (Main.DEBUG)
						System.out.println("Nouvelle connexion initiée au port " + distantPort);

					// On ouvre une connexion TCP entre les deux PacketManager. On lui envoie un
					// port libre PAR TCP !
					try {
						Socket client = new Socket(address, distantPort);
						DataOutputStream out = new DataOutputStream(client.getOutputStream());
						DataInputStream in = new DataInputStream(client.getInputStream());

						out.writeUTF("Coucou bro !");

						String message = in.readUTF();

						if (Main.DEBUG)
							System.out.println("[Server]: Message reçu : " + message);
						
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
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

	private PacketToReceive readPacket(int idPacket) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		Class<? extends PacketToReceive> clazz = idToPacket.get(idPacket);
		PacketToReceive packet = clazz.getDeclaredConstructor().newInstance();

		packet.initFromStream(inputStream);
		if (Main.DEBUG)
			System.out.println("Packet fully read: " + packet);

		return packet;
	}

	public void sendPacket(PacketToEmit packet) {
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
		byte[] buf;
		PacketManager.getInstance().setPort(1234);

		ServerSocket serverSocket = new ServerSocket(1236);
		
		buf = "1236".getBytes();
		DatagramSocket socket = new DatagramSocket();
		DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), 1234);
		socket.send(packet);
		socket.close();
		
		Socket s = serverSocket.accept();
		DataOutputStream out = new DataOutputStream(s.getOutputStream());
		DataInputStream in = new DataInputStream(s.getInputStream());
		
		System.out.println("[Client]: Message reçu : " + in.readUTF());
		out.writeUTF("Salut !");

	}

}
