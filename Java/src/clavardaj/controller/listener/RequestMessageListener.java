package clavardaj.controller.listener;

import clavardaj.model.Agent;

public interface RequestMessageListener {

	void onRequestMessage(Agent agent);

	void onRequestAllMessages(Agent agent);

}
