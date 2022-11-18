package clavardaj.controller;

import java.util.ArrayList;
import java.util.List;

import clavardaj.controller.listener.LoginChangeListener;
import clavardaj.controller.listener.LoginListener;
import clavardaj.controller.listener.MessageListener;
import clavardaj.model.Agent;

public class ListenerManager {

	private static final ListenerManager instance = new ListenerManager();

	private List<LoginListener> loginListenerList;
	private List<LoginChangeListener> loginChangeListenerList;
	private List<MessageListener> messageListenerList;
	
	private ListenerManager() {
		loginListenerList = new ArrayList<>();
		loginChangeListenerList = new ArrayList<>();
		messageListenerList = new ArrayList<>();
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
	
	public void fireAgentLogin(Agent agent) {
		loginListenerList.forEach(listener -> listener.onAgentLogin(agent));
	}
	
	public void fireAgentLogout(Agent agent) {
		loginListenerList.forEach(listener -> listener.onAgentLogout(agent));
	}
	
	public void fireAgentLoginChange(Agent agent, String newLogin) {
		loginListenerList.forEach(listener -> listener.onAgentLogout(agent));
	}
	
	public void fireMessageReceived(Agent agent, String message) {
		messageListenerList.forEach(listener -> listener.onMessageReceived(agent, message));
	}
	
	public static ListenerManager getInstance() {
		return instance;
	}
}
