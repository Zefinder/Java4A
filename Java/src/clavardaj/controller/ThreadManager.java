package clavardaj.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import clavardaj.controller.listener.ConversationListener;
import clavardaj.controller.listener.MessageToTransferListener;
import clavardaj.model.Agent;
import clavardaj.model.ClientThread;
import clavardaj.model.Message;
import clavardaj.model.ServerThread;
import clavardaj.model.UserThread;

public class ThreadManager implements MessageToTransferListener, ConversationListener {

	Map<Agent, UserThread> conversations = new HashMap<Agent, UserThread>();
	private static final ThreadManager instance = new ThreadManager();

	private ThreadManager() {
		ListenerManager.getInstance().addConversationListener(this);
		ListenerManager.getInstance().addMessageToTransferListener(this);
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
		conversations.get(agent).close();
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
		conversations.get(agent).close();
	}

	@Override
	public void onMessageToSend(Agent agent, String message) {
		conversations.get(agent).write(message);
	}

	@Override
	public void onMessageToReceive(Agent agent) {
		Message message = conversations.get(agent).read(agent);
		ListenerManager.getInstance().fireMessageReceived(agent, message);
	}

	public static ThreadManager getInstance() {
		return instance;
	}

	public static void main(String[] args) throws IOException {
//		InetAddress ip = InetAddress.getLocalHost();
//		Agent agent2 = new Agent(1, ip, 1743);
//		System.out.println("[Manager] création thread");
//		ListenerManager.getInstance().fireConversationOpening(agent2, 1742);
//		System.out.println("[Manager] thread créés");
//		System.out.println("[Manager] envoi message");
//		ListenerManager.getInstance().fireMessageToSend(agent2, "Coucou bebou");

		InetAddress ip = InetAddress.getLocalHost();
		Agent agent1 = new Agent(UUID.randomUUID(), ip, 1742, "Bébou");
		System.out.println("[Manager] création thread");
		ListenerManager.getInstance().fireConversationOpened(agent1);
		System.out.println("[Manager] thread créés");
		ListenerManager.getInstance().fireMessageToReceive(agent1);
	}

}
