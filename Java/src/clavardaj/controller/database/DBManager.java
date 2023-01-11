package clavardaj.controller.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import clavardaj.controller.UserManager;
import clavardaj.model.Agent;
import clavardaj.model.Message;

public abstract class DBManager {

	protected Connection connection;
	protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	protected UserManager umanager = UserManager.getInstance();

	public abstract boolean init(String instanceName, String login, String password) throws SQLException;

	public abstract void addMessage(Message message) throws SQLException;

	/**
	 * Adds a user in the user's table
	 * 
	 * @param agent  the agent to put in the table
	 * @param passwd the password of the agent if it is the main agent
	 * @throws SQLException if a database access error occurs
	 */
	public abstract void addUser(UUID uuid, String name, String passwd) throws SQLException;

	/**
	 * Select all messages sent to or by the specified agent.
	 * 
	 * @param agent the agent to get the messages from
	 * @return the list of all messages
	 * @throws SQLException if a database access error occurs
	 * 
	 * @see Message
	 */
	public abstract List<? extends Message> requestMessages(UUID uuid) throws SQLException;

	/**
	 * Select the last message sent to or by the specified agent
	 * 
	 * @param agent the agent to get the messages from
	 * @return the last message sent by the agent
	 * @throws SQLException if a database access error occurs
	 */
	public abstract Message requestMessage(UUID uuid) throws SQLException;

	/**
	 * Check if the login and password match to any line of the user table.
	 * 
	 * @param login  the agent's login
	 * @param passwd the agent's password
	 * @return the agent that matches (or null if none matches)
	 * @throws SQLException
	 */
	public abstract Agent checkUser(String login, String passwd) throws SQLException;

	public void closeConnection() throws SQLException {
		connection.close();
	}

}
