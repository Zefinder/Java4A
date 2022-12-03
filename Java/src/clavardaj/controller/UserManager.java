package clavardaj.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.UUID;

import clavardaj.controller.listener.LoginChangeListener;
import clavardaj.controller.listener.LoginListener;
import clavardaj.model.Agent;

public class UserManager implements LoginListener, LoginChangeListener {

	private static final UserManager instance = new UserManager();

	private List<Agent> agentList;
	private Agent currentAgent;

	public Agent getCurrentAgent() {
		return currentAgent;
	}

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
	public void onSelfLogin(UUID uuid, String name) {
		try {
			this.currentAgent = new Agent(uuid, InetAddress.getLocalHost(), name);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onSelfLogout() {
		// TODO Auto-generated method stub

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

}
