package clavardaj.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

public class ClientThread extends UserThread {

	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;

	public ClientThread(Socket socket) {
		super(socket);
	}

	@Override
	public void close() {
		try {

			this.out.close();
			this.in.close();
			this.socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
