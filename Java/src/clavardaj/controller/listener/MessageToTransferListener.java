package clavardaj.controller.listener;

import clavardaj.model.Agent;

public interface MessageToTransferListener {

	void onMessageToSend(Agent agent, String string);
	
	/**
	 * @param agent is the agent that sent the message
	 */
	void onMessageToReceive(Agent agent);
	
}
