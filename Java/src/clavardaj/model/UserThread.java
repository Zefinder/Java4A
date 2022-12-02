package clavardaj.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * 
 * @author nicolas
 *
 */
public abstract class UserThread {

	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;

	public UserThread(Socket socket) {

		this.socket = socket;
		try {
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Message read(Agent sender) {
		Message message;
		String content = new String();
		try {
			content = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		message = new Message(content, sender);

		return message;
	}

	public void write(String message) {
		try {
			System.out.println("envoi message : " + message);
			out.write(message + "\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public abstract void close();
}
