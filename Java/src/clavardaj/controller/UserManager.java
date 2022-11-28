package clavardaj.controller;

import java.util.List;

import clavardaj.controller.listener.LoginListener;
import clavardaj.model.Agent;

public class UserManager implements LoginListener {

	private static final UserManager instance = new UserManager();
	
	private List<Agent> agentList;
	private Agent currentAgent;
	
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

	@Override
	public void onSelfLogin() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSelfLogout() {
		// TODO Auto-generated method stub
		
	}
}
