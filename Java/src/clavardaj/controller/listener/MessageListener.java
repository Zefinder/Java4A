package clavardaj.controller.listener;

import clavardaj.controller.ListenerManager;
import clavardaj.model.Message;

/**
 * <p>
 * The listener interface for receiving messages events. The class that is
 * interested in processing a message event implements this interface, and the
 * object created (or singleton initialized) with that class is registered using
 * the {@link ListenerManager}'s {@code addMessageListener()} method.
 * </p>
 * 
 * @see ListenerManager
 */
public interface MessageListener {

	/**
	 * Invoked when a message is received by a conversation socket
	 * 
	 * @param message the received {@link Message}
	 */
	void onMessageReceived(Message message);

	/**
	 * Invoked when a message is sent by a conversation socket
	 * 
	 * @param message the sent {@link Message}
	 */
	void onMessageSent(Message message);

}
