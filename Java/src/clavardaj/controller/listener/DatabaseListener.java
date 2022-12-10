package clavardaj.controller.listener;

import java.util.List;

import clavardaj.controller.ListenerManager;
import clavardaj.model.Message;

/**
 * <p>
 * The listener interface for receiving database transfers events. The class
 * that is interested in processing a database transfer event implements this
 * interface, and the object created (or singleton initialized) with that class
 * is registered using the {@link ListenerManager}'s
 * {@code addDatabaseListener()} method.
 * </p>
 * 
 * @see ListenerManager
 */
public interface DatabaseListener {

	/**
	 * Invoked when the database sends the requested message.
	 * 
	 * @param message the requested {@link Message}
	 */
	void onMessageTransfered(Message message);

	/**
	 * Invoked when the database sends all requested messages.
	 * 
	 * @param messages a list of requested {@link Message}
	 */
	void onAllMessagesTransfered(List<Message> messages);

}
