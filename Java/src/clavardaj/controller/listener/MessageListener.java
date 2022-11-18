package clavardaj.controller.listener;

import clavardaj.model.Agent;

public interface MessageListener {
	
	void onMessageReceived(Agent agent, String message);

}
