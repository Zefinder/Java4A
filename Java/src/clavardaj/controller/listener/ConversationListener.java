package clavardaj.controller.listener;

import clavardaj.model.Agent;

public interface ConversationListener {
	
	void onConversationOpening(Agent agent, int localPort);
	
	void onConversationClosing(Agent agent);
	
	void onConversationOpened(Agent agent);
	
	void onConversationClosed(Agent agent);

}
