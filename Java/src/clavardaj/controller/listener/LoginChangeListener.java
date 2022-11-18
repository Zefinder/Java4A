package clavardaj.controller.listener;

import clavardaj.model.Agent;

public interface LoginChangeListener {

	void fireAgentLoginChange(Agent agent, String newLogin);
	
}
