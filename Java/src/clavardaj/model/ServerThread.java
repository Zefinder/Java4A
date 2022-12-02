package clavardaj.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends UserThread {

	private Socket socket;
	private ServerSocket serverSocket;
	private BufferedReader in;
	private BufferedWriter out;
	
	public ServerThread(Socket socket, ServerSocket serverSocket) {
		super(socket);
		this.serverSocket = serverSocket;
	}

	@Override
	public void close() {
		try {

			this.out.close();
			this.in.close();
			this.socket.close();
			this.serverSocket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
