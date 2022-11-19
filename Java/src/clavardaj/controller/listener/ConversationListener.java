package clavardaj.controller.listener;

import clavardaj.model.Agent;

public interface ConversationListener {
	
	void onConversationOpened(Agent agent);
	
	void onConversationClosed(Agent agent);

}
