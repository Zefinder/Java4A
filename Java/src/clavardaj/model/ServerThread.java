package clavardaj.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends UserThread {

	private ServerSocket serverSocket;

	public ServerThread(Socket socket, ServerSocket serverSocket) {
		super(socket);
		this.serverSocket = serverSocket;
	}

	@Override
	public void close() throws IOException {
		super.close();
		this.serverSocket.close();
	}

}
