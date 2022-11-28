package clavardaj.controller.listener;

import clavardaj.model.Agent;

public interface LoginListener {

	void onAgentLogin(Agent agent);

	void onAgentLogout(Agent agent);
	
	void onSelfLogin();
	
	void onSelfLogout();
	
}
