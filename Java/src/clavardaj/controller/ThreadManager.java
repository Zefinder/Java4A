package clavardaj.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import clavardaj.controller.listener.ConversationListener;
import clavardaj.controller.listener.MessageToTransferListener;
import clavardaj.model.Agent;
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
			
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			UserThread thread = new UserThread(agent, localPort, in, out);
			
			System.out.println("[Serveur] thread créé");
			
			conversations.put(agent, thread);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConversationClosing(Agent agent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConversationOpened(Agent agent) {
		try {

			System.out.println("[Client] connexion au port " + agent.getPort());
			
			Socket socket = new Socket(agent.getIp().getHostAddress(), agent.getPort());
			
			System.out.println("[Client] connecté");

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			UserThread thread = new UserThread(agent, agent.getPort(), in, out);
			
			System.out.println("[Client] thread créé");
			
			conversations.put(agent, thread);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConversationClosed(Agent agent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessageToSend(Agent agent, String message) {
		conversations.get(agent).write(message);
	}

	@Override
	public void onMessageToReceive(Agent agent) {
		String message = conversations.get(agent).read();
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
		Agent agent1 = new Agent(0, ip, 1742);
		System.out.println("[Manager] création thread");
		ListenerManager.getInstance().fireConversationOpened(agent1);
		System.out.println("[Manager] thread créés");
		ListenerManager.getInstance().fireMessageToReceive(agent1);
	}
	
}
