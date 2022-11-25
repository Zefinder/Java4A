package clavardaj.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public class ClientThread extends UserThread {

	public ClientThread(Agent agent, int port, BufferedReader in, BufferedWriter out) {
		super(agent, port, in, out);
	}

	@Override
	public void setup() {
		// TODO Auto-generated method stub
		
	}


}
