package clavardaj.model;

import java.net.InetAddress;
import java.util.UUID;

public class Agent {

	private UUID uuid;
	private InetAddress ip;
	private int port;
	private String name;

	public Agent(UUID uuid, InetAddress ip, String name) {
		this.uuid = uuid;
		this.ip = ip;
		this.name = name;
	}

	public Agent(UUID uuid, InetAddress ip, int port, String name) {
		this.uuid = uuid;
		this.ip = ip;
		this.port = port;
		this.name = name;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return String.format("%s (%s)", name, uuid.toString());
	}
	
	@Override
	public boolean equals(Object autre) {
		return uuid.equals(((Agent) autre).getUuid());
	}
	
}
