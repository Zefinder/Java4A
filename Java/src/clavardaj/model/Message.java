package clavardaj.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Message {

	private Agent sender;
	private Agent receiver;
	private LocalDateTime date;

	public Message(Agent sender, Agent receiver, LocalDateTime date) {
		this.sender = sender;
		this.receiver = receiver;
		this.date = date;
	}

	public abstract byte[] getContent();

	public Agent getSender() {
		return sender;
	}

	public LocalDateTime getDate() {
		return date;
	}

	public Agent getReceiver() {
		return receiver;
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
		return String.format("[%s] %s -> %s", date.format(formatter), sender.toString(), receiver.toString());
	}

}
