package clavardaj.model;

import java.time.LocalDateTime;

public class FileMessage extends Message{
	
	private String fileName;

	public FileMessage(String fileName, String content, Agent sender, Agent receiver, LocalDateTime date) {
		super(content, sender, receiver, date);
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	@Override
	public String toString() {
		return super.toString() + String.format(" (file name = %s)", fileName);
	}

}
