package clavardaj.model;

import java.net.InetAddress;
import java.util.UUID;

import clavardaj.controller.UserManager;

/**
 * <p>
 * Represents a user. He is defined by his {@link UUID}, {@link InetAddress} and
 * name.
 * </p>
 * <p>
 * On connection of this user, an agent will be created and registered as the
 * "current agent" or "main agent" in the {@link UserManager}
 * </p>
 * <p>
 * On connection of a distant user, an agent will be created and stocked in a
 * list of distant agents in {@link UserManager}
 * </p>
 * <p>
 * All informations on agents are written in the database and can change in time
 * (except the UUID which is unique)
 * </p>
 * 
 * @see UserManager
 * @see UUID
 */
public class Agent {

	private final UUID uuid;
	private InetAddress ip;
	private int port; // <- local port!!
	private String name;

	
	/**
	 * Creates a new {@link Agent} with a specified UUID, IP Address and name. 
	 * 
	 * @param uuid the unique {@link UUID} of the agent
	 * @param ip the IP address of the agent
	 * @param name the name of the agent
	 */
	public Agent(UUID uuid, InetAddress ip, String name) {
		this.uuid = uuid;
		this.ip = ip;
		this.port = 0;
		this.name = name;
	}

	public UUID getUuid() {
		return uuid;
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
