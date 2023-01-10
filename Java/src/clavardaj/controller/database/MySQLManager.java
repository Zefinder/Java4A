package clavardaj.controller.database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import clavardaj.controller.DBManagerAdapter;
import clavardaj.model.Agent;
import clavardaj.model.FileMessage;
import clavardaj.model.Message;
import clavardaj.model.TextMessage;

/**
 * <p>
 * This manager is in charge of the MySQL database and is used by the
 * {@link DBManagerAdapter}
 * </p>
 * 
 * @author Adrien Jakubiak
 * 
 * @see DBManagerAdapter
 * @see SQLiteManager
 * 
 * @since 1.0.0
 *
 */
public class MySQLManager extends DBManager {

	public MySQLManager() {
	}

	@Override
	public boolean init(String instanceName, String login, String password) throws SQLException {
		DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
		ResultSet resultSet;

		// Connection à la DB
		connection = DriverManager.getConnection("jdbc:mysql://localhost/" + instanceName, login, password);
		java.sql.Statement statement = connection.createStatement();

		// Vérification de l'existance des tables (sinon création)
		resultSet = statement.executeQuery(
				"SELECT DISTINCT `TABLE_NAME` FROM `INFORMATION_SCHEMA`. `COLUMNS` WHERE `table_schema` = 'clavardaj';");

		boolean user = false, message = false;

		while (resultSet.next()) {
			switch (resultSet.getString(1)) {
			case "user":
				user = true;
				break;

			case "message":
				message = true;
				break;
			default:
				break;
			}
		}

		if (!user) {
			statement.execute(
					"CREATE TABLE `clavardaj`.`user` ( `uuid` VARCHAR(36) NOT NULL , `login` VARCHAR(20) NOT NULL , `passwd` VARCHAR(32) NULL , PRIMARY KEY (`uuid`(36)));");
		}

		if (!message) {
			statement.execute(
					"CREATE TABLE `clavardaj`.`message` ( `userSend` VARCHAR(36) NOT NULL , `userRcv` VARCHAR(36) NOT NULL , `date` DATETIME NOT NULL , `nano` INT NOT NULL , `content` VARCHAR(2048) NOT NULL , `isFile` INT NOT NULL , PRIMARY KEY (`userSend`, `userRcv`, `date`, `nano`));");
		}

		resultSet.close();
		statement.close();
		
		return true;
	}

	@Override
	public void addMessage(Message message) throws SQLException {
		java.sql.Statement statement = connection.createStatement();
		String content;
		int isFile = 0;

		if (message instanceof TextMessage)
			content = new String(message.getContent());
		else {
			content = ((FileMessage) message).getFileName();
			isFile = 1;
		}

		statement.execute(String.format(
				"INSERT INTO `message` (`userSend`, `userRcv`, `date`, `nano`, `content`, `isFile`) VALUES ('%s', '%s', '%s', %d, '%s', %d);",
				message.getSender().getUuid(), message.getReceiver().getUuid(), message.getDate().format(formatter),
				message.getDate().getNano(), content, isFile));

		statement.close();
	}

	/**
	 * Adds a user in the user's table
	 * 
	 * @param agent  the agent to put in the table
	 * @param passwd the password of the agent if it is the main agent
	 * @throws SQLException if a database access error occurs
	 */
	@Override
	public void addUser(UUID uuid, String name, String passwd) throws SQLException {
		java.sql.Statement statement = connection.createStatement();
		statement.execute(String.format("INSERT INTO `user` (`uuid`, `login`, `passwd`) VALUES ('%s', '%s', '%s');",
				uuid, name, passwd));

		statement.close();
	}

	/**
	 * Select all messages sent to or by the specified agent.
	 * 
	 * @param agent the agent to get the messages from
	 * @return the list of all messages
	 * @throws SQLException if a database access error occurs
	 * 
	 * @see Message
	 */
	@Override
	public List<? extends Message> requestMessages(UUID uuid) throws SQLException {
		java.sql.Statement statement = connection.createStatement();
		List<Message> messages = new ArrayList<>();

		ResultSet resultSet = statement.executeQuery(String
				.format("SELECT `message`.* FROM `message` WHERE userSend = '%s' OR userRcv = '%s';", uuid, uuid));

		while (resultSet.next()) {
			String content = resultSet.getString("content");
			UUID userSend = UUID.fromString(resultSet.getString("userSend"));
			UUID userRcv = UUID.fromString(resultSet.getString("userRcv"));
			LocalDateTime date = LocalDateTime.parse(resultSet.getString("date"), formatter);
			int isFile = Integer.parseInt(resultSet.getString("isFile"));

			if (isFile == 0)
				messages.add(new TextMessage(content, umanager.getAgentByUuid(userSend),
						umanager.getAgentByUuid(userRcv), date));
			else
				messages.add(new FileMessage(content, null, umanager.getAgentByUuid(userSend),
						umanager.getAgentByUuid(userRcv), date));
		}

		resultSet.close();
		statement.close();

		return messages;
	}

	/**
	 * Select the last message sent to or by the specified agent
	 * 
	 * @param agent the agent to get the messages from
	 * @return the last message sent by the agent
	 * @throws SQLException if a database access error occurs
	 */
	@Override
	public Message requestMessage(UUID uuid) throws SQLException {
		java.sql.Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(String.format(
				"SELECT `message`.* FROM `message` WHERE userSend = '%s' OR userRcv = '%s' ORDER BY nano DESC;", uuid,
				uuid));

		if (resultSet.next()) {
			String content = resultSet.getString("content");
			UUID userSend = UUID.fromString(resultSet.getString("userSend"));
			UUID userRcv = UUID.fromString(resultSet.getString("userRcv"));
			LocalDateTime date = LocalDateTime.parse(resultSet.getString("date"), formatter);
			int isFile = Integer.parseInt(resultSet.getString("isFile"));

			resultSet.close();
			statement.close();

			if (isFile == 0)
				return new TextMessage(content, umanager.getAgentByUuid(userSend), umanager.getAgentByUuid(userRcv),
						date);
			else
				return new FileMessage(content, null, umanager.getAgentByUuid(userSend),
						umanager.getAgentByUuid(userRcv), date);
		} else
			return null;
	}

	/**
	 * Check if the login and password match to any line of the user table.
	 * 
	 * @param login  the agent's login
	 * @param passwd the agent's password
	 * @return the agent that matches (or null if none matches)
	 * @throws SQLException
	 */
	@Override
	public Agent checkUser(String login, String passwd) throws SQLException {
		java.sql.Statement statement = connection.createStatement();
		ResultSet resultSet = statement.executeQuery(
				String.format("SELECT `user`.* FROM `user` WHERE login = '%s' AND passwd = '%s';", login, passwd));

		if (resultSet.next()) {
			UUID uuid = UUID.fromString(resultSet.getString("uuid"));
			return new Agent(uuid, null, login);
		}

		resultSet.close();
		statement.close();
		return null;
	}

}