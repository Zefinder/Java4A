package clavardaj.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;


/**
 * 
 * @author nicolas
 *
 */
public abstract class UserThread {

	private Agent agent;
	private int serverPort;
	private BufferedReader in;
	private BufferedWriter out;

	public UserThread(Agent agent, int serverPort, BufferedReader in, BufferedWriter out) {
		this.agent = agent;
		this.serverPort = serverPort;
		this.in = in;
		this.out = out;
	}
	
	public String read() {
		String message;
		try {
			 message = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			message = "";
		}
		return message;
	}
	
	public void write(String message) {
		try {
			out.write(message);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
