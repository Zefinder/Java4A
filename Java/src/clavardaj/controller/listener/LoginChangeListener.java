package clavardaj.controller.listener;

import clavardaj.model.Agent;

public interface LoginChangeListener {

	void onAgentLoginChange(Agent agent, String newLogin);
	
	void onSelfLoginChange(String newLogin);
	
}
