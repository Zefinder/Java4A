package clavardaj.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import clavardaj.controller.listener.ConversationListener;
import clavardaj.controller.listener.MessageToTransferListener;
import clavardaj.model.Agent;
import clavardaj.model.ClientThread;
import clavardaj.model.ServerThread;

public class ThreadManager implements MessageToTransferListener, ConversationListener {

	Map<Agent, ServerThread> servers = new HashMap<Agent, ServerThread>();
	Map<Agent, ClientThread> clients = new HashMap<Agent, ClientThread>();

	@Override
	public void onConversationOpening(Agent agent) {
		try {

			ServerSocket serverSocket = new ServerSocket(agent.getPort());
			Socket socket = serverSocket.accept();

			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

			ServerThread server = new ServerThread(agent, agent.getPort(), in, out);
			servers.put(agent, server);

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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConversationClosed(Agent agent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessageToSend(Agent agent, String string) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMessageToReceive(Agent agent) {
		// TODO Auto-generated method stub

	}
}
