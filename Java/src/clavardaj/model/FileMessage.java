package clavardaj.model;

import java.time.LocalDateTime;

public class FileMessage extends Message {
	
	private String fileName;
	private byte[] content;

	public FileMessage(String fileName, byte[] content, Agent sender, Agent receiver, LocalDateTime date) {
		super(sender, receiver, date);
		this.content = content;
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public byte[] getContent() {
		return content;
	}
	
	@Override
	public String toString() {
		return super.toString() + String.format(" (file name = %s)", fileName);
	}

}
