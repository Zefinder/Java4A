package clavardaj.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class MachineA {
	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket socket = new Socket("localhost", 1234);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		System.out.println(in.readLine());
		socket.close();
	}
}
