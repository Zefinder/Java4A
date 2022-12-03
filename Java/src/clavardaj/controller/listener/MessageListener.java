package clavardaj.controller.listener;

import clavardaj.model.Message;

public interface MessageListener {
	
	void onMessageReceived(Message message);

	void onMessageSent(Message message);

}
