package clavardaj.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public class ServerThread extends UserThread {

	public ServerThread(Agent agent, int port, BufferedReader in, BufferedWriter out) {
		super(agent, port, in, out);
	}

}
