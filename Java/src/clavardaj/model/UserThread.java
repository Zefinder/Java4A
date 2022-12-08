package clavardaj.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;

import clavardaj.controller.UserManager;

/**
 * 
 * @author nicolas
 *
 */
public abstract class UserThread {

	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	public UserThread(Socket socket) {
		this.socket = socket;
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Message read(Agent sender) {
		Message message;
		String content = "";
		try {
			content = in.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		message = new Message(content, sender, UserManager.getInstance().getCurrentAgent(), LocalDateTime.now());

		return message;
	}

	public void write(String message) {
		try {
			out.writeUTF(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		socket.close();
		in.close();
		out.close();
	}
}
