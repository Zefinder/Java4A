package clavardaj.model;

import java.io.IOException;
import java.net.Socket;

public class ClientThread extends UserThread {

	public ClientThread(Socket socket) {
		super(socket);
	}

	@Override
	public void close() throws IOException {
	}

}
