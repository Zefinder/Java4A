package clavardaj.controller.listener;

import clavardaj.controller.ListenerManager;
import clavardaj.model.Agent;

/**
 * <p>
 * The listener interface for receiving request to database events. The class
 * that is interested in processing a request to database event implements this
 * interface, and the object created (or singleton initialized) with that class
 * is registered using the {@link ListenerManager}'s
 * {@code addDatabaseListener()} method.
 * </p>
 * 
 * @see ListenerManager
 */
public interface RequestMessageListener {

	/**
	 * <p>
	 * Invoked when a message concerning an agent is requested.
	 * </p>
	 * <em>This will make a request for the last message concerning this agent</em>
	 * 
	 * @param agent the concerned {@link Agent}
	 */
	void onRequestMessage(Agent agent);

	/**
	 * Invoked when all messages concerning an agent are requested.
	 * 
	 * @param agent the concerned {@link Agent}
	 */
	void onRequestAllMessages(Agent agent);

}
