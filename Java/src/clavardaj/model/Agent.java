package clavardaj.model;

import java.net.InetAddress;

public class Agent {

	private int uid;
	private InetAddress ip;
	private int port;
	
	public Agent(int uid, InetAddress ip, int port) {
		this.uid = uid;
		this.ip = ip;
		this.port = port;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public InetAddress getIp() {
		return ip;
	}

	public void setIp(InetAddress ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
