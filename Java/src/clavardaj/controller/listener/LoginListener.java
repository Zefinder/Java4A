package clavardaj.controller.listener;

import java.util.UUID;

import clavardaj.model.Agent;

public interface LoginListener {

	void onAgentLogin(Agent agent);

	void onAgentLogout(Agent agent);
	
	void onSelfLogin(UUID uuid, String name);
	
	void onSelfLogout();
	
}
