package clavardaj.model;

import java.time.LocalDateTime;

public class Message {

	private String content;
	private Agent sender;
	private Agent receiver;
	private LocalDateTime date;

	public Message(String content, Agent sender, Agent receiver, LocalDateTime date) {
		this.content = content;
		this.sender = sender;
		this.receiver = receiver;
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public Agent getSender() {
		return sender;
	}

	public Agent getReceiver() {
		return receiver;
	}

	public LocalDateTime getDate() {
		return date;
	}

}
