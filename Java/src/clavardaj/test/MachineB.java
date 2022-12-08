package clavardaj.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MachineB {
	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(1234);
		Socket socket = server.accept();
		
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		
		out.write("Coucou !\n");
		out.flush();
		
		server.close();
		socket.close();
	}
}
