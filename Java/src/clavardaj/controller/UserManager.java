package clavardaj.controller;

import java.util.List;

import clavardaj.controller.listener.LoginListener;
import clavardaj.model.Agent;

public class UserManager implements LoginListener {

	private static final UserManager instance = new UserManager();
	
	List<Agent> agentList;
	Agent currentAgent;
	
	private UserManager() {
		
	}

	public static UserManager getInstance() {
		return instance;
	}
	
	@Override
	public void onAgentLogin(Agent agent) {
		if (!agentList.contains(agent))
			agentList.add(agent);
	}

	@Override
	public void onAgentLogout(Agent agent) {
		agentList.remove(agent);
	}
}
