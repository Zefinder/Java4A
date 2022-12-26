package clavardaj.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class FileMessage extends Message {
	
	private String fileName;
	private byte[] content;

	public FileMessage(String fileName, byte[] content, UUID uuidSender, UUID uuidReceiver, LocalDateTime date) {
		super(uuidSender, uuidReceiver, date);
		this.content = content;
		this.fileName = fileName;
	}
	
	public FileMessage(String fileName, byte[] content, UUID uuidSender, UUID uuidReceiver) {
		super(uuidSender, uuidReceiver);
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
