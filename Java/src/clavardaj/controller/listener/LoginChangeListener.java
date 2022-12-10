package clavardaj.controller.listener;

import clavardaj.controller.ListenerManager;
import clavardaj.model.Agent;

/**
 * <p>
 * The listener interface for receiving login change events. The class that is
 * interested in processing a login change event implements this interface, and
 * the object created (or singleton initialized) with that class is registered
 * using the {@link ListenerManager}'s {@code addLoginChangeListener()} method.
 * </p>
 * 
 * @see ListenerManager
 */
public interface LoginChangeListener {

	/**
	 * Invoked when a distant agent changed his login.
	 * 
	 * @param agent    the {@link Agent} whose login changed
	 * @param newLogin the new login of the agent
	 */
	void onAgentLoginChange(Agent agent, String newLogin);

	/**
	 * Invoked when the main agent changed his login.
	 * 
	 * @param newLogin the new login of the main agent
	 */
	void onSelfLoginChange(String newLogin);

}
