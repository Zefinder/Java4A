package clavardaj.model;

import java.time.LocalDateTime;

public class Message {

	private String content;
	private Agent sender;
	private LocalDateTime date;

	public Message(String content, Agent sender) {
		this.content = content;
		this.sender = sender;
		this.date = LocalDateTime.now();
	}

	public Message(String content, Agent sender, LocalDateTime date) {
		this.content = content;
		this.sender = sender;
		this.date = date;
	}
	
	public String getContent() {
		return content;
	}

	public Agent getSender() {
		return sender;
	}

	public LocalDateTime getDate() {
		return date;
	}

}
