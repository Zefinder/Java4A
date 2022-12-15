package clavardaj.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.UUID;
import java.time.format.DateTimeFormatter;

public abstract class Message {

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

	public abstract byte[] getContent();

	public UUID getUuidSender() {
		return uuidSender;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public UUID getUuidReceiver() {
		return uuidReceiver;
	}

	private static byte[] parseFile(File file) throws IOException {
		System.out.println(file.length());
		byte[] content = Files.readAllBytes(file.toPath());
		System.out.println(content.length);
		return content;
	}

	public static Message createFileMessage(File file, Agent sender, Agent receiver) throws IOException {
		return new FileMessage(file.getName(), parseFile(file), sender, receiver, LocalDateTime.now());
	}

	@Override
	public String toString() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		return String.format("[%s] %s -> %s", date.format(formatter), uuidSender.toString(), uuidReceiver.toString());
	}

}
