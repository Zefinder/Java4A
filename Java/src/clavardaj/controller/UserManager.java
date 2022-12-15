package clavardaj.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import clavardaj.controller.listener.LoginChangeListener;
import clavardaj.controller.listener.LoginListener;
import clavardaj.model.Agent;

/**
 * <p>
 * Manager used to identify the logged agents as well as the current
 * {@link Agent} (called main agent).
 * </p>
 * 
 * <p>
 * It is also used to get an {@link Agent} by his IP or his {@link UUID}.
 * </p>
 * 
 * <p>
 * This manager is implemented as a singleton, to access it, use the
 * {@link #getInstance()} method.
 * </p>
 * 
 * @see #getInstance()
 * @see Agent
 * @see PacketManager
 * @see ThreadManager
 * @see DBManager
 * @see ListenerManager
 * 
 * @author Nicolas Rigal
 * 
 * @since 1.0.0
 *
 */
public class UserManager implements LoginListener, LoginChangeListener {

	private static final UserManager instance = new UserManager();

	private List<Agent> agentList;
	private Agent currentAgent;

	private UserManager() {
		agentList = new ArrayList<>();

		ListenerManager.getInstance().addLoginListener(this);
		ListenerManager.getInstance().addLoginChangeListener(this);
	}

	/**
	 * Find the {@link Agent} in the list of agents with his UUID
	 * 
	 * @param uuid the agent's {@link UUID}
	 * @return the agent with the same UUID, or null if not found
	 */
	public Agent getAgentByUuid(UUID uuid) {
		for (Agent agent : agentList) {
			if (agent.getUuid().equals(uuid))
				return agent;
		}
		return null;
	}

	/**
	 * Find the {@link Agent} in the list of agents with his IP
	 * 
	 * @param ip the agent's {@link InetAddress}
	 * @return the agent with the same IP, or null if not found
	 */
	public Agent getAgentByIP(InetAddress ip) {
		for (Agent agent : agentList) {
			if (agent.getIp().equals(ip))
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

	/**
	 * Get the instance of the manager
	 * 
	 * @return the manager's instance
	 * 
	 * @see UserManager
	 */
	public static UserManager getInstance() {
		return instance;
	}
}
