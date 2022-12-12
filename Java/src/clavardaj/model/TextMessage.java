package clavardaj.model;

import java.time.LocalDateTime;

public class TextMessage extends Message{

	public TextMessage(String content, Agent sender, Agent receiver, LocalDateTime date) {
		super(content, sender, receiver, date);
	}

}
