package clavardaj.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class TextMessage extends Message{

	private String content;
	
	public TextMessage(String content, UUID uuidSender, UUID uuidReceiver, LocalDateTime date) {
		super(uuidSender, uuidReceiver, date);
		this.content = content;
	}
	
	@Override
	public byte[] getContent() {
		return content.getBytes();
	}
	
	public String getStringContent() {
		return content;
	}

}
