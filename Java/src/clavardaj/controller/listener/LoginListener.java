package clavardaj.controller.listener;

import java.util.UUID;

import clavardaj.controller.ListenerManager;
import clavardaj.model.Agent;

/**
 * <p>
 * The listener interface for receiving login events. The class that is
 * interested in processing a login event implements this interface, and the
 * object created (or singleton initialized) with that class is registered using
 * the {@link ListenerManager}'s {@code addLoginListener()} method.
 * </p>
 * 
 * @see ListenerManager
 */
public interface LoginListener {

	/**
	 * Invoked when a distant agent logs to this agent.
	 * 
	 * @param agent the {@link Agent} that logs in.
	 */
	void onAgentLogin(Agent agent);

	/**
	 * Invoked when a distant agent logs out to this agent.
	 * 
	 * @param agent the {@link Agent} that logs out.
	 */
	void onAgentLogout(Agent agent);

	/**
	 * Invoked when the main agent logs in.
	 * 
	 * @param uuid the {@link UUID} of the main agent
	 * @param name the name of the main agent
	 */
	void onSelfLogin(UUID uuid, String name, String password);

	/**
	 * Invoked when the main agent logs out.
	 */
	void onSelfLogout();

}
