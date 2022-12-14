package clavardaj.model;

import java.time.LocalDateTime;

public class TextMessage extends Message{

	private String content;
	
	public TextMessage(String content, Agent sender, Agent receiver, LocalDateTime date) {
		super(sender, receiver, date);
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
