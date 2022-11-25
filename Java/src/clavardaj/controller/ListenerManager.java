package clavardaj.controller;

import java.util.ArrayList;
import java.util.List;

import clavardaj.controller.listener.ConversationListener;
import clavardaj.controller.listener.DatabaseListener;
import clavardaj.controller.listener.LoginChangeListener;
import clavardaj.controller.listener.LoginListener;
import clavardaj.controller.listener.MessageListener;
import clavardaj.controller.listener.MessageToTransferListener;
import clavardaj.controller.listener.RequestMessageListener;
import clavardaj.model.Agent;
import clavardaj.model.Message;

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

	public void fireAgentLogin(Agent agent) {
		loginListenerList.forEach(listener -> listener.onAgentLogin(agent));
	}

	public void fireAgentLogout(Agent agent) {
		loginListenerList.forEach(listener -> listener.onAgentLogout(agent));
	}

	public void fireSelfLogin() {
		loginListenerList.forEach(LoginListener::onSelfLogin);
	}

	public void fireSelfLogout() {
		loginListenerList.forEach(LoginListener::onSelfLogout);
	}

	public void fireAgentLoginChange(Agent agent, String newLogin) {
		loginChangeListenerList.forEach(listener -> listener.onAgentLoginChange(agent, newLogin));
	}

	public void fireSelfLoginChange(String newLogin) {
		loginChangeListenerList.forEach(listener -> listener.onSelfLoginChange(newLogin));
	}

	public void fireMessageReceived(Agent agent, String message) {
		messageListenerList.forEach(listener -> listener.onMessageReceived(agent, message));
	}

	public void fireMessageSent(Agent agent, String message) {
		messageListenerList.forEach(listener -> listener.onMessageSent(agent, message));
	}

	public void fireMessageToSend(Agent agent, String message) {
		messageToTransferListenerList.forEach(listener -> listener.onMessageToSend(agent, message));
	}

	public void fireMessageToReceive(Agent agent) {
		messageToTransferListenerList.forEach(listener -> listener.onMessageToReceive(agent));
	}

	public void fireConversationOpening(Agent agent) {
		conversationListenerList.forEach(listener -> listener.onConversationOpening(agent));
	}

	public void fireConversationClosing(Agent agent) {
		conversationListenerList.forEach(listener -> listener.onConversationClosing(agent));
	}

	public void fireConversationOpened(Agent agent) {
		conversationListenerList.forEach(listener -> listener.onConversationOpened(agent));
	}

	public void fireConversationClosed(Agent agent) {
		conversationListenerList.forEach(listener -> listener.onConversationClosed(agent));
	}

	public void fireMessageTransfered(Message message) {
		databaseListenerList.forEach(listener -> listener.onMessageTransfered(message));
	}

	public void fireAllMessagesTransfered(List<Message> messages) {
		databaseListenerList.forEach(listener -> listener.onAllMessagesTransfered(messages));
	}

	public void fireRequestMessage(Agent agent) {
		requestMessageListenerList.forEach(listener -> listener.onRequestMessage(agent));
	}

	public void fireRequestAllMessages(Agent agent) {
		requestMessageListenerList.forEach(listener -> listener.onRequestAllMessages(agent));
	}

	public static ListenerManager getInstance() {
		return instance;
	}
}
