package clavardaj.model;

public class Message {

	String contenu;
	Agent sender;

	public Message(String contenu, Agent sender) {
		this.contenu = contenu;
		this.sender = sender;
	}

}
