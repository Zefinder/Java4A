package clavardaj.controller.listener;

import clavardaj.controller.ListenerManager;
import clavardaj.model.Agent;
import clavardaj.model.Message;

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

	/**
	 * Invoked when the main agent wants to send a message to a distant agent.
	 * 
	 * @param agent   the distant {@link Agent} to send the message to
	 * @param message the message
	 */
	void onMessageToSend(Agent agent, Message message);

	/**
	 * Invoked when the main agent needs to receive a message from a distant agent.
	 * 
	 * @param agent  the {@link Agent} who sent the message
	 * @param isFile true if the message is a file
	 */
	void onMessageToReceive(Agent agent, boolean isFile);

}
