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

	public Message read(Agent sender, boolean isFile) {
		Message message = null;
		String content = "";
		String fileName = "";
		try {
			if (isFile) {
				fileName = in.readUTF();
				content = in.readUTF();
				message = new FileMessage(fileName, content, sender, UserManager.getInstance().getCurrentAgent(),
						LocalDateTime.now());
			} else {
				content = in.readUTF();
				message = new TextMessage(content, sender, UserManager.getInstance().getCurrentAgent(),
						LocalDateTime.now());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return message;
	}

	public void write(Message message) {
		try {
			if (message instanceof FileMessage)
				out.writeUTF(((FileMessage) message).getFileName());

			out.writeUTF(message.getContent());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(message);
	}

	public void close() throws IOException {
		socket.close();
		in.close();
		out.close();
	}
}
