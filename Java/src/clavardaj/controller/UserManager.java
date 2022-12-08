package clavardaj.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import clavardaj.controller.listener.LoginChangeListener;
import clavardaj.controller.listener.LoginListener;
import clavardaj.model.Agent;

public class UserManager implements LoginListener, LoginChangeListener {

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
	public void onSelfLogin(UUID uuid, String name) {
		try {
			this.currentAgent = new Agent(uuid, InetAddress.getLocalHost(), name);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSelfLogout() {
	}

	@Override
	public void onAgentLoginChange(Agent agent, String newLogin) {
		if (!agentList.contains(agent))
			agentList.add(agent);
		
		for (Agent e : agentList) {
			if (e.equals(agent)) {
				e.setName(newLogin);
				break;
			}
		}
	}

	@Override
	public void onSelfLoginChange(String newLogin) {
		currentAgent.setName(newLogin);
	}

	public Agent getCurrentAgent() {
		return currentAgent;
	}
}
