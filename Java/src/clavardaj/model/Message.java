package clavardaj.model;

import java.util.Date;

public class Message {

	private String contenu;
	private Agent sender;
	private Date date;

	public Message(String contenu, Agent sender, Date date) {
		this.contenu = contenu;
		this.sender = sender;
		this.date = date;
	}

}
