package clavardaj.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import clavardaj.controller.listener.ConversationListener;
import clavardaj.controller.listener.DatabaseListener;
import clavardaj.controller.listener.LoginChangeListener;
import clavardaj.controller.listener.LoginListener;
import clavardaj.controller.listener.MessageListener;
import clavardaj.controller.listener.MessageToTransferListener;
import clavardaj.controller.listener.RequestMessageListener;
import clavardaj.model.Agent;
import clavardaj.model.Message;

/**
 * <p>
 * Manager used to send messages through Listeners. It allows more flexibility
 * to the code making it easier to modify and to add new functionalities in the
 * future.
 * </p>
 * 
 * <p>
 * Allowing a class to receive events from this manager is easy:
 * <ul>
 * <li>The class must implement a listener</li>
 * <li>The class must register itself to the ListenerManager</li>
 * </ul>
 * </p>
 * 
 * <p>
 * This manager is implemented as a singleton, to access it, use the
 * {@link #getInstance()} method.
 * </p>
 * 
 * @author Nicolas Rigal
 *
 * @see #getInstance()
 * @see ThreadManager
 * @see UserManager
 * @see DBManager
 * @see PacketManager
 * 
 * @since 1.0.0
 * 
 */
public class ListenerManager {

	private static final ListenerManager instance = new ListenerManager();

	private List<LoginListener> loginListenerList;
	private List<LoginChangeListener> loginChangeListenerList;
	private List<MessageListener> messageListenerList;
	private List<ConversationListener> conversationListenerList;
	private List<DatabaseListener> databaseListenerList;
	private List<MessageToTransferListener> messageToTransferListenerList;
	private List<RequestMessageListener> requestMessageListenerList;

	private ListenerManager() {
		loginListenerList = new ArrayList<>();
		loginChangeListenerList = new ArrayList<>();
		messageListenerList = new ArrayList<>();
		conversationListenerList = new ArrayList<>();
		databaseListenerList = new ArrayList<>();
		messageToTransferListenerList = new ArrayList<>();
		requestMessageListenerList = new ArrayList<>();
	}

	public void addLoginListener(LoginListener listener) {
		loginListenerList.add(listener);
	}

	public void addLoginChangeListener(LoginChangeListener listener) {
		loginChangeListenerList.add(listener);
	}

	public void addMessageListener(MessageListener listener) {
		messageListenerList.add(listener);
	}

	public void addConversationListener(ConversationListener listener) {
		conversationListenerList.add(listener);
	}

	public void addDatabaseListener(DatabaseListener listener) {
		databaseListenerList.add(listener);
	}

	public void addMessageToTransferListener(MessageToTransferListener listener) {
		messageToTransferListenerList.add(listener);
	}

	public void addRequestMessageListener(RequestMessageListener listener) {
		requestMessageListenerList.add(listener);
	}

	/**
	 * Notifies that an agent has logged onto the network. This should notify :
	 * <ul>
	 * <li>DBManager</li>
	 * </ul>
	 * 
	 * @param agent is the agent that just logged in
	 */
	public void fireAgentLogin(Agent agent) {
		loginListenerList.forEach(listener -> listener.onAgentLogin(agent));
	}

	/**
	 * Notifies that an agent has logged out of the network. This should notify :
	 * <ul>
	 * <li>DBManager</li>
	 * </ul>
	 * 
	 * @param agent is the agent that just logged out
	 */
	public void fireAgentLogout(Agent agent) {
		loginListenerList.forEach(listener -> listener.onAgentLogout(agent));
	}

	/**
	 * Notifies that the local agent has logged in in order to broadcast the
	 * information to other agents. This should notify :
	 * <ul>
	 * <li>PacketManager</li>
	 * </ul>
	 */
	public void fireSelfLogin(UUID uuid, String name) {
		loginListenerList.forEach(listener -> listener.onSelfLogin(uuid, name));
	}

	/**
	 * Notifies that the local agent has logged out in order to broadcast the
	 * information to other agents. This should notify :
	 * <ul>
	 * <li>PacketManager</li>
	 * </ul>
	 */
	public void fireSelfLogout() {
		loginListenerList.forEach(LoginListener::onSelfLogout);
	}

	/**
	 * Notifies that an agent has changed its login in order to update its local
	 * representation. This should notify :
	 * <ul>
	 * <li>PacketManager</li>
	 * <li>DBManager</li>
	 * </ul>
	 * 
	 * @param agent    is the distant agent that changed its login
	 * @param newLogin is the new login the distant agent chose
	 */
	public void fireAgentLoginChange(Agent agent, String newLogin) {
		loginChangeListenerList.forEach(listener -> listener.onAgentLoginChange(agent, newLogin));
	}

	/**
	 * Notifies that the local agent has changed its login in order to broadcast the
	 * change to other agents. This should notify :
	 * <ul>
	 * <li>PacketManager</li>
	 * <li>DBManager</li>
	 * </ul>
	 * 
	 * @param newLogin is the new login to send to the other agents
	 */
	public void fireSelfLoginChange(String newLogin) {
		loginChangeListenerList.forEach(listener -> listener.onSelfLoginChange(newLogin));
	}

	/**
	 * Notifies that a message has been received so that it can be added in the
	 * database. This should not be mistaken with fireMessageToReceive which notify
	 * that a message is pending. This should notify :
	 * <ul>
	 * <li>DBManager</li>
	 * </ul>
	 * 
	 * @param agent   is the distant agent that sent the message
	 * @param message is the message that has been received
	 */
	public void fireMessageReceived(Message message) {
		messageListenerList.forEach(listener -> listener.onMessageReceived(message));
	}

	/**
	 * Notifies that a message has been sent so that it can be added in the
	 * database. This should notify :
	 * <ul>
	 * <li>DBManager</li>
	 * </ul>
	 * 
	 * @param agent   is the agent that sent a message
	 * @param message is the sent message
	 */
	public void fireMessageSent(Message message) {
		messageListenerList.forEach(listener -> listener.onMessageSent(message));
	}

	/**
	 * Notifies that a message is pending in the corresponding output buffer so that
	 * the thread can send it. This should notify :
	 * <ul>
	 * <li>ThreadManager</li>
	 * </ul>
	 * 
	 * @param agent   is the agent that sent a message
	 * @param message is the message to send
	 */
	public void fireMessageToSend(Agent agent, Message message) {
		messageToTransferListenerList.forEach(listener -> listener.onMessageToSend(agent, message));
	}

	/**
	 * Notifies that a message is pending in the corresponding input buffer so that
	 * the thread can retrieve it. This should notify :
	 * <ul>
	 * <li>ThreadManager</li>
	 * </ul>
	 * 
	 * @param agent is the agent that sent a message
	 * @param isFile 
	 */
	public void fireMessageToReceive(Agent agent, boolean isFile) {
		messageToTransferListenerList.forEach(listener -> listener.onMessageToReceive(agent, isFile));
	}

	/**
	 * Notifies that the local agent is opening a conversation with a distant agent.
	 * This should notify :
	 * <ul>
	 * <li>DBManager</li>
	 * <li>ThreadManager</li>
	 * </ul>
	 * 
	 * @param agent is the distant agent
	 */
	public void fireConversationOpening(Agent agent, int localPort) {
		conversationListenerList.forEach(listener -> listener.onConversationOpening(agent, localPort));
	}

	/**
	 * Notifies that the local agent is closing a conversation with a distant agent.
	 * This should notify :
	 * <ul>
	 * <li>DBManager</li>
	 * <li>ThreadManager</li>
	 * </ul>
	 * 
	 * @param agent is the distant agent
	 */
	public void fireConversationClosing(Agent agent) {
		conversationListenerList.forEach(listener -> listener.onConversationClosing(agent));
	}

	/**
	 * Notifies that a distant agent has opened a conversation with the local agent.
	 * This should notify :
	 * <ul>
	 * <li>DBManager</li>
	 * <li>ThreadManager</li>
	 * </ul>
	 * 
	 * @param agent is the distant agent
	 */
	public void fireConversationOpened(Agent agent) {
		conversationListenerList.forEach(listener -> listener.onConversationOpened(agent));
	}

	/**
	 * Notifies that a distant agent has closed a conversation with the local agent.
	 * This should notify :
	 * <ul>
	 * <li>DBManager</li>
	 * <li>ThreadManager</li>
	 * </ul>
	 * 
	 * @param agent is the distant agent
	 */
	public void fireConversationClosed(Agent agent) {
		conversationListenerList.forEach(listener -> listener.onConversationClosed(agent));
	}

	public void fireMessageTransfered(Message message) {
		databaseListenerList.forEach(listener -> listener.onMessageTransfered(message));
	}

	public void fireAllMessagesTransfered(List<Message> messages) {
		databaseListenerList.forEach(listener -> listener.onAllMessagesTransfered(messages));
	}

	/**
	 * Sends a request for the last stored message exchanged with an agent to the
	 * database. This should notify :
	 * <ul>
	 * <li>DBManager</li>
	 * </ul>
	 * 
	 * @param agent is the agent with which the message was exchanged
	 */
	public void fireRequestMessage(Agent agent) {
		requestMessageListenerList.forEach(listener -> listener.onRequestMessage(agent));
	}

	/**
	 * Sends a request for all stored messages exchanged with an agent to the
	 * database. This should notify :
	 * <ul>
	 * <li>DBManager</li>
	 * </ul>
	 * 
	 * @param agent is the agent with which the messages was exchanged
	 */
	public void fireRequestAllMessages(Agent agent) {
		requestMessageListenerList.forEach(listener -> listener.onRequestAllMessages(agent));
	}

	/**
	 * Get the instance of the manager
	 * 
	 * @return the manager's instance
	 * 
	 * @see ListenerManager
	 */
	public static ListenerManager getInstance() {
		return instance;
	}
}
