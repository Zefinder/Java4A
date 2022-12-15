package clavardaj.controller;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import clavardaj.Main;
import clavardaj.controller.listener.ConversationListener;
import clavardaj.controller.listener.MessageToTransferListener;
import clavardaj.model.Agent;
import clavardaj.model.ClientThread;
import clavardaj.model.Message;
import clavardaj.model.ServerThread;
import clavardaj.model.UserThread;

/**
 * <p>
 * Manager used to create and manage conversation between this agent and a
 * distant agent.
 * </p>
 * 
 * <p>
 * This manager is implemented as a singleton, to access it, use the
 * {@link #getInstance()} method.
 * </p>
 * 
 * @see #getInstance()
 * @see Agent
 * @see UserThread
 * @see PacketManager
 * @see UserManager
 * @see DBManager
 * @see ListenerManager
 * 
 * @author Nicolas Rigal
 * 
 * @since 1.0.0
 *
 */
public class ThreadManager implements MessageToTransferListener, ConversationListener {

	private Map<Agent, UserThread> conversations;
	private static final ThreadManager instance = new ThreadManager();

	private ThreadManager() {
		ListenerManager.getInstance().addConversationListener(this);
		ListenerManager.getInstance().addMessageToTransferListener(this);
		this.conversations = new HashMap<Agent, UserThread>();

		if (!Main.OUTPUT.exists())
			Main.OUTPUT.mkdir();
	}

	@Override
	public void onConversationOpening(Agent agent, int localPort) {
		try {

			System.out.println("[Serveur] accept port " + localPort);

			ServerSocket serverSocket = new ServerSocket(localPort);
			Socket socket = serverSocket.accept();

			System.out.println("[Serveur] connexion acceptée");

			UserThread thread = new ServerThread(socket, serverSocket);

			System.out.println("[Serveur] thread créé");

			conversations.put(agent, thread);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConversationClosing(Agent agent) {
		try {
			conversations.get(agent).close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConversationOpened(Agent agent) {
		try {

			System.out.println("[Client] connexion au port " + agent.getPort());

			Socket socket = new Socket(agent.getIp().getHostAddress(), agent.getPort());

			System.out.println("[Client] connecté");

			UserThread thread = new ClientThread(socket);

			System.out.println("[Client] thread créé");

			conversations.put(agent, thread);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConversationClosed(Agent agent) {
		try {
			conversations.get(agent).close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageToSend(Agent agent, Message message) {
		System.out.println("[ThreadManager] Message to send");
		conversations.get(agent).write(message);
	}

	@Override
	public void onMessageToReceive(Agent agent, boolean isFile) {
		System.out.println("[ThreadManager] Message to receive");
		UserThread userThread = conversations.get(agent);

		Message message = userThread.read(agent, isFile);
		ListenerManager.getInstance().fireMessageReceived(message);
	}

	/**
	 * Get the instance of the manager
	 * 
	 * @return the manager's instance
	 * 
	 * @see ThreadManager
	 */
	public static ThreadManager getInstance() {
		return instance;
	}
}
