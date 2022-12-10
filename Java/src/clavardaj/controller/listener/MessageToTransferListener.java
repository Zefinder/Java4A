package clavardaj.controller.listener;

import clavardaj.controller.ListenerManager;
import clavardaj.model.Agent;

/**
 * <p>
 * The listener interface for receiving messages to transfer events. The class
 * that is interested in processing a message to transfer event implements this
 * interface, and the object created (or singleton initialized) with that class
 * is registered using the {@link ListenerManager}'s
 * {@code addMessageToTransferListener()} method.
 * </p>
 * 
 * @see ListenerManager
 */
public interface MessageToTransferListener {

	// TODO Changer Agent et String en Message pour tout contenir sans tout modifier
	// en cas de changement de structure !
	/**
	 * Invoked when the main agent wants to send a message to a distant agent.
	 * 
	 * @param agent  the distant {@link Agent} to send the message to
	 * @param string the message
	 */
	void onMessageToSend(Agent agent, String string);

	/**
	 * Invoked when the main agent needs to receive a message from a distant agent.
	 * 
	 * @param agent the {@link Agent} who sent the message
	 */
	void onMessageToReceive(Agent agent);

}
