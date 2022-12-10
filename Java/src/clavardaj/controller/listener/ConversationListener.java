package clavardaj.controller.listener;

import clavardaj.controller.ListenerManager;
import clavardaj.model.Agent;

/**
 * <p>
 * The listener interface for receiving conversation events. The class that is
 * interested in processing a conversation event implements this interface, and
 * the object created (or singleton initialized) with that class is registered
 * using the {@link ListenerManager}'s {@code addConversationListener} method.
 * </p>
 * 
 * @see ListenerManager
 */
public interface ConversationListener {

	/**
	 * Invoked when a non-existent conversation needs to be opened.
	 * 
	 * @param agent     the {@link Agent} whom conversation needs to be opened with
	 * @param localPort the TCP port to listen on
	 */
	void onConversationOpening(Agent agent, int localPort);

	/**
	 * Invoked when a opened conversation is needed to be closed.
	 * 
	 * @param agent the {@link Agent} whose conversation needs to be closed
	 */
	void onConversationClosing(Agent agent);

	/**
	 * Invoked when a conversation is opened on a distant agent.
	 * 
	 * @param agent the {@link Agent} whose conversation is opened
	 */
	void onConversationOpened(Agent agent);

	/**
	 * Invoked when a conversation is closed on a distant agent.
	 * 
	 * @param agent the {@link Agent} whose conversation is closed
	 */
	void onConversationClosed(Agent agent);

}
