package clavardaj.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import clavardaj.controller.listener.LoginListener;
import clavardaj.model.Agent;

public class UserManager implements LoginListener {

	private static final UserManager instance = new UserManager();
	
	private List<Agent> agentList;
	private Agent currentAgent;
	
	private UserManager() {
		agentList = new ArrayList<>();
		
		ListenerManager.getInstance().addLoginListener(this);
	}

	public static UserManager getInstance() {
		return instance;
	}
	
	public Agent getAgentByUuid(UUID uuid) {
		for (Agent agent : agentList) {
			if (agent.getUuid().equals(uuid))
				return agent;
		}
		return null;
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

	public Agent getCurrentAgent() {
		return currentAgent;
	}
}
