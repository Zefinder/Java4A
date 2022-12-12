package clavardaj.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Message {

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

	public LocalDateTime getDate() {
		return date;
	}

	public Agent getReceiver() {
		return receiver;
	}

	private static String parseFile(File file) throws IOException {
		FileInputStream stream = new FileInputStream(file);
		String content = new String(stream.readAllBytes());
		stream.close();
		
		return content;
	}
	
	public static Message createFileMessage(File file, Agent sender, Agent receiver) throws IOException {
		return new FileMessage(file.getName(), parseFile(file), sender, receiver, LocalDateTime.now());
	}

	@Override
	public String toString() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		return String.format("[%s] %s -> %s : %s", date.format(formatter), sender.toString(), receiver.toString(),
				content);
	}

}
