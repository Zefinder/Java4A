package clavardaj.controller.listener;

import java.util.List;

import clavardaj.model.Message;

public interface DatabaseListener {
	
	void onMessageTransfered(Message message);
	
	void onAllMessagesTransfered(List<Message> messages);

}
