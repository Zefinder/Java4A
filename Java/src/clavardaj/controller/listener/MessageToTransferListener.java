package clavardaj.controller.listener;

import clavardaj.model.Agent;

public interface MessageToTransferListener {

	void onMessageToSend(Agent agent, String string);
	
	void onMessageToReceive(Agent agent);
	
}
