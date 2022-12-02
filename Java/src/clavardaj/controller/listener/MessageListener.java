package clavardaj.controller.listener;

import clavardaj.model.Agent;
import clavardaj.model.Message;

public interface MessageListener {
	
	void onMessageReceived(Agent agent, Message message);
	
	void onMessageSent(Agent agent, String message);

}
