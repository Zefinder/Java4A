package clavardaj.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Message {

	private String content;
	private UUID uuidSender;
	private UUID uuidReceiver;
	private LocalDateTime date;

	public Message(String content, UUID uuidSender, UUID uuidReceiver, LocalDateTime date) {
		this.content = content;
		this.uuidSender = uuidSender;
		this.uuidReceiver = uuidReceiver;
		this.date = date;
	}
	
	public String getContent() {
		return content;
	}

	public UUID getUuidSender() {
		return uuidSender;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public UUID getUuidReceiver() {
		return uuidReceiver;
	}

	@Override
	public String toString() {
		return String.format("[%s] %s -> %s : %s", date.toString(), uuidSender.toString(), uuidReceiver.toString(), content);
	}

}
