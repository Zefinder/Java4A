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
 * <p>
 * Manager used to send packets to the distant agents' {@link PacketManager}, to
 * receive TCP connections and UDP packets from other agents.
 * </p>
 * 
 * <p>
 * This manager's strategy for connection is pretty simple :
 * <ul>
 * <li>A UDP packet is received containing a TCP port to connect to on the
 * distant agent.</li>
 * <li>A TCP connection is established where a server port is negotiated. This
 * is done to get a privileged communication non connected to other sockets so
 * we do not mix packets.</li>
 * <li>Once the 2nd TCP connection is established, a {@link PacketEmtLogin} is
 * sent to give this {@link Agent}'s UUID and current login. In the meantime, we
 * receive a {@link PacketEmtLogin} with the distant agent's UUID and name.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * For each established connection between two {@link PacketManager}, a
 * {@link PacketThread} is created to receive packets.
 * </p>
 * 
 * <p>
 * This manager is implemented as a singleton, to access it, use the
 * {@link #getInstance()} method.
 * </p>
 *
 * @see #getInstance()
 * @see PacketThread
 * @see TCPServerThread
 * @see UDPServerThread
 * @see PacketToEmit
 * @see PacketToReceive
 * @see ThreadManager
 * @see UserManager
 * @see DBManager
 * @see ListenerManager
 * 
 * @author Adrien Jakubiak
 * 
 * @since 1.0.0
 *
 */
public class PacketManager implements LoginListener {

	private static final PacketManager instance = new PacketManager();

	private final Map<Integer, Class<? extends PacketToReceive>> idToPacket;
	private final Map<Class<? extends PacketToEmit>, Integer> packetToId;
	private final Map<InetAddress, Socket> ipToSocket;

	private List<InetAddress> localAddresses;
	private List<InetAddress> broadcastAddresses;

	private DatagramSocket UDPserver;
	private static final BlockingQueue<PacketToReceive> packetsToHandle = new LinkedBlockingQueue<>();

	/**
	 * UDP listening DatagramSocket port
	 */
	private final int UDP_PORT = 1233;

	/**
	 * TCP listening ServerSocket port
	 */
	private final int TCP_PORT = 1234;

	private ServerSocket TCPserver;

	private int nextAvailablePort;

	private PacketManager() {
		nextAvailablePort = TCP_PORT;
		localAddresses = new ArrayList<>();
		broadcastAddresses = new ArrayList<>();

		idToPacket = new HashMap<>();
		packetToId = new HashMap<>();
		ipToSocket = new HashMap<>();

		// On récupère toutes les adresses correspondantes à cette machine
		try {
			Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaceEnumeration.hasMoreElements()) {
				for (InterfaceAddress interfaceAddress : networkInterfaceEnumeration.nextElement()
						.getInterfaceAddresses())
					if (interfaceAddress.getAddress().isSiteLocalAddress()) {
						localAddresses.add(interfaceAddress.getAddress());
						broadcastAddresses.add(interfaceAddress.getBroadcast());
					}
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
		new Thread(new TCPServerThread(), "TCP Server").start();

		// On lance le thread d'écoute UDP
		new Thread(new UDPServerThread(), "UDP Server").start();

		// On lance le process de paquets
		new Thread(new ProcessPacketThread(), "Packet processing").start();

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
		for (InetAddress broadcastAddress : broadcastAddresses) {
			DatagramPacket packet = new DatagramPacket(buf, buf.length, broadcastAddress, UDP_PORT);
			socket.send(packet);
		}

		socket.close();
	}

	/**
	 * Send a packet to another agent using the specified
	 * {@linkplain DataOutputStream}.
	 * 
	 * @deprecated As the other manager do not know the output stream but the IP
	 *             address of a distant agent, this function isn't useful anymore.
	 *             It's likely recommended to use the new other
	 *             {@link #sendPacket(InetAddress, PacketToEmit)}, using the IP
	 *             address.
	 * 
	 * @param outputStream the stream to send the packet on
	 * @param packet       the packet to send to the distant agent
	 * 
	 * @see PacketToEmit
	 * @see #sendPacket(InetAddress, PacketToEmit)
	 */
	@Deprecated
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

	/**
	 * Sends the packet to the agent specified by its IP address.
	 * 
	 * @param ip     the distant agent's ip to send the packet to
	 * @param packet the packet to send to the distant agent
	 * 
	 * @see Agent
	 * @see PacketToEmit
	 */
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

	/**
	 * Get the next available port to connect a socket on
	 * 
	 * @return the next available port before incrementing it
	 */
	public int getNextAvailablePort() {
		return nextAvailablePort++;
	}

	/**
	 * Get the instance of the manager
	 * 
	 * @return the manager's instance
	 * 
	 * @see PacketManager
	 */
	public static PacketManager getInstance() {
		return instance;
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
			TCPserver.close();
			UDPserver.close();
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

	/**
	 * <p>
	 * This represents the TCP server waiting for connections. For each connection
	 * to {@link PacketManager#TCP_PORT} it will :
	 * <ul>
	 * <li>Send a new port on which it'll be possible to connect on</li>
	 * <li>Wait for the new connection</li>
	 * <li>Launch a {@link PacketThread} with the socket's
	 * {@link DataInputStream}</li>
	 * <li>Associate the IP address with the socket</li>
	 * <li>Send a {@link PacketEmtLogin} with this agent UUID and name</li>
	 * </ul>
	 * </p>
	 * 
	 * @see UDPServerThread
	 * @see PacketThread
	 * @see PacketManager
	 * 
	 * @author Adrien Jakubiak
	 *
	 */
	private class TCPServerThread implements Runnable {

		@Override
		public void run() {
			try {
				// Serveur global de redirection TCP

				if (Main.DEBUG)
					System.out.println("[Server]: TCP door opened!");
				TCPserver = new ServerSocket(nextAvailablePort++);

				while (true) {
					Socket socket = TCPserver.accept();

					if (Main.DEBUG)
						System.out.println(
								"[Server]: Client connected... Redirecting to port " + nextAvailablePort + "!");

					ServerSocket newServer = null;
					while (newServer == null)
						try {
							newServer = new ServerSocket(nextAvailablePort);
						} catch (IOException e) {
							System.err.println("[PacketManager] Port already used for newServer");
						}

					new DataOutputStream(socket.getOutputStream()).writeInt(nextAvailablePort++);
					socket.close();

					socket = newServer.accept();

					if (Main.DEBUG)
						System.out.println("[Server]: Client redirected, ready to transfer packets!");

					DataInputStream in = new DataInputStream(socket.getInputStream());

					// On lance l'écoute de paquets pour TCP
					new Thread(new PacketThread(in), "Packet read").start();
					newServer.close();

					// On envoie un login packet à la machine distante avec notre nom !
					if (socket.getInetAddress().toString().equals("/127.0.0.1"))
						for (InetAddress ip : localAddresses)
							ipToSocket.put(ip, socket);

					ipToSocket.put(socket.getInetAddress(), socket);

					Agent agent = UserManager.getInstance().getCurrentAgent();
					sendPacket(socket.getInetAddress(),
							new PacketEmtLogin(agent.getUuid(), socket.getLocalAddress(), agent.getName()));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * <p>
	 * This represents the UDP server waiting for UDP packets. For each packet to
	 * {@link PacketManager#UDP_PORT} it will :
	 * <ul>
	 * <li>Read the packet and verify that the packet is not from us nor from an
	 * already known user</li>
	 * <li>Connect via TCP to the specified port and get the new port to connect
	 * on</li>
	 * <li>Connect via TCP to the new received port</li>
	 * <li>Launch a {@link PacketThread} with the socket's
	 * {@link DataInputStream}</li>
	 * <li>Associate the IP address with the socket</li>
	 * <li>Send a {@link PacketEmtLogin} with this agent UUID and name</li>
	 * </ul>
	 * </p>
	 * 
	 * @author Adrien Jakubiak
	 * 
	 * @see TCPServerThread
	 * @see PacketThread
	 * @see PacketManager
	 *
	 */
	private class UDPServerThread implements Runnable {
		private byte[] buf = new byte[5];

		@Override
		public void run() {

			try {
				UDPserver = new DatagramSocket(UDP_PORT);
			} catch (SocketException e1) {
				e1.printStackTrace();
			}

			while (true) {
				// On prend le paquet UDP
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				try {
					UDPserver.receive(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}

				// On dissèque le paquet
				InetAddress address = packet.getAddress();
				int port = packet.getPort();

				if (Main.DEBUG)
					System.out.println("[Server]: UDP Packet received from address " + address.getHostAddress());

				// On regarde si le paquet ne vient pas de nous...
				boolean cancel = false;
				for (InetAddress localAddress : localAddresses)
					if (address.equals(localAddress)) {
						cancel = true;
						if (Main.DEBUG)
							System.err.println("[Server]: UDP Packet was from us!");
					}

				if (!cancel)
					for (InetAddress connected : ipToSocket.keySet())
						if (address.equals(connected)) {
							cancel = true;
							if (Main.DEBUG)
								System.err.println("[Server]: UDP Packet was from an already known agent!");
						}

				if (cancel)
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
					new Thread(new PacketThread(in), "Packet read").start();

					ipToSocket.put(client.getInetAddress(), client);

					// On envoie un login packet à la machine distante avec notre nom !
					Agent agent = UserManager.getInstance().getCurrentAgent();
					sendPacket(client.getInetAddress(),
							new PacketEmtLogin(agent.getUuid(), client.getLocalAddress(), agent.getName()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * <p>
	 * This thread will wait for a {@link PacketToReceive} in order to read it and
	 * process it later. This {@link DataInputStream} is linked to the
	 * {@link DataOutputStream} of the distant agent, sending the packet via
	 * {@link PacketManager#sendPacket(InetAddress, PacketToEmit)}
	 * </p>
	 * 
	 * <p>
	 * Once the packet is receive and initialized, it's put in a
	 * {@link BlockingQueue} in order to get processed. This allows this thread to
	 * process all packets he needs to receive.
	 * </p>
	 * 
	 * @author Adrien Jakubiak
	 * 
	 * @see PacketManager
	 * @see TCPServerThread
	 * @see UDPServerThread
	 * @see ProcessPacketThread
	 */
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
					packetsToHandle.add(packet);
				} catch (IOException | InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e) {

					if (e instanceof IOException) {
						if (Main.DEBUG)
							System.out.println("[PacketThread] Socket closed");

						for (InetAddress ip : ipToSocket.keySet()) {
							Socket socket = ipToSocket.get(ip);
							try {
								if (inputStream.equals(socket.getInputStream())) {
									Agent agent = UserManager.getInstance().getAgentByIP(ip);
									ListenerManager.getInstance().fireAgentLogout(agent);
								}
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					} else
						e.printStackTrace();

					break;

				}
		}
	}

	/**
	 * This thread processes {@link PacketToReceive} that are in the blocking queue.
	 * 
	 * @author Adrien Jakubiak
	 */
	private class ProcessPacketThread implements Runnable {

		@Override
		public void run() {
			PacketToReceive packet = null;

			while (true) {
				try {
					packet = packetsToHandle.take();
					packet.processPacket();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
