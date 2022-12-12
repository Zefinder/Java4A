package clavardaj.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import clavardaj.controller.listener.LoginListener;
import clavardaj.controller.listener.MessageListener;
import clavardaj.model.Agent;
import clavardaj.model.Message;
import clavardaj.model.TextMessage;

/**
 * <p>
 * This manager is in charge of the MySQL database. Most of its methods are to
 * use directly without passing with a listener as they are situational
 * ({@link #checkUser(String, String)} as an example).
 * </p>
 * 
 * <p>
 * The database is composed of 2 main tables
 * <ul>
 * <li>user, containing the main user and the distant users</li>
 * <li>message, containing all messages sent from or to this user</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Only the main agent has a password, the others have a null instead.
 * </p>
 * 
 * <p>
 * This manager is implemented as a singleton, to access it, use the
 * {@link #getInstance()} method.
 * </p>
 * 
 * @author Adrien Jakubiak
 * 
 * @see #getInstance()
 * @see PacketManager
 * @see ThreadManager
 * @see UserManager
 * @see ListenerManager
 * 
 * @since 1.0.0
 *
 */
public class DBManager implements LoginListener, MessageListener {

	private static final DBManager instance = new DBManager();
	private Connection connection;
	private java.sql.Statement statement;
	private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	private UserManager umanager = UserManager.getInstance();

	private DBManager() {
		ListenerManager.getInstance().addLoginListener(this);
		ListenerManager.getInstance().addMessageListener(this);

		// Initialisation de la base de donnée
		try {
			init();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void init() throws SQLException {
		ResultSet resultSet;
		DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());

		// Connection à la DB
		connection = DriverManager.getConnection("jdbc:mysql://localhost/clavardaj", "root", "");
		statement = connection.createStatement();

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
					"CREATE TABLE `clavardaj`.`message` ( `userSend` VARCHAR(36) NOT NULL , `userRcv` VARCHAR(36) NOT NULL , `date` DATETIME NOT NULL , `content` VARCHAR(2048) NOT NULL , PRIMARY KEY (`userSend`, `userRcv`, `date`));");
		}

		resultSet.close();
	}

	private void addMessage(Message message) throws SQLException {
		statement.execute(String.format(
				"INSERT INTO `message` (`userSend`, `userRcv`, `date`, `content`) VALUES ('%s', '%s', '%s', '%s');",
				message.getSender().getUuid(), message.getReceiver().getUuid(), message.getDate().format(formatter),
				message.getContent()));
	}

	/**
	 * Adds a user in the user's table
	 * 
	 * @param agent  the agent to put in the table
	 * @param passwd the password of the agent if it is the main agent
	 * @throws SQLException if a database access error occurs
	 */
	public void addUser(Agent agent, String passwd) throws SQLException {
		statement.execute(String.format("INSERT INTO `user` (`uuid`, `login`, `passwd`) VALUES ('%s', '%s', '%s');",
				agent.getUuid(), agent.getName(), passwd));
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
	public List<? extends Message> requestMessages(Agent agent) throws SQLException {
		List<Message> messages = new ArrayList<>();

		ResultSet resultSet = statement.executeQuery(
				String.format("SELECT `message`.* FROM `message` WHERE userSend = '%s' OR userRcv = '%s';",
						agent.getUuid(), agent.getUuid()));

		while (resultSet.next()) {
			String content = resultSet.getString("content");
			UUID userSend = UUID.fromString(resultSet.getString("userSend"));
			UUID userRcv = UUID.fromString(resultSet.getString("userRcv"));
			LocalDateTime date = LocalDateTime.parse(resultSet.getString("date"), formatter);

			messages.add(
					new TextMessage(content, umanager.getAgentByUuid(userSend), umanager.getAgentByUuid(userRcv), date));
		}

		resultSet.close();
		return messages;
	}

	/**
	 * Select the last message sent to or by the specified agent
	 * 
	 * @param agent the agent to get the messages from
	 * @return the last message sent by the agent
	 * @throws SQLException if a database access error occurs
	 */
	public Message requestMessage(Agent agent) throws SQLException {
		ResultSet resultSet = statement.executeQuery(String.format(
				"SELECT `message`.* FROM `message` WHERE userSend = '1544080c-8643-41cb-a2be-2962ce842b8a' OR userRcv = '1544080c-8643-41cb-a2be-2962ce842b8a' ORDER BY date DESC;",
				agent.getUuid(), agent.getUuid()));

		resultSet.next();

		String content = resultSet.getString("content");
		UUID userSend = UUID.fromString(resultSet.getString("userSend"));
		UUID userRcv = UUID.fromString(resultSet.getString("userRcv"));
		LocalDateTime date = LocalDateTime.parse(resultSet.getString("date"), formatter);

		resultSet.close();
		return new TextMessage(content, umanager.getAgentByUuid(userSend), umanager.getAgentByUuid(userRcv), date);
	}

	/**
	 * Check if the login and password match to any line of the user table.
	 * 
	 * @param login the agent's login
	 * @param passwd the agent's password
	 * @return the agent that matches (or null if none matches)
	 * @throws SQLException
	 */
	public Agent checkUser(String login, String passwd) throws SQLException {
		// SELECT * FROM user WHERE login = 'a' AND passwd = 'A';

		ResultSet resultSet = statement.executeQuery(
				String.format("SELECT `user`.* FROM `user` WHERE login = '%s' AND passwd = '%s';", login, passwd));

		if (resultSet.next()) {
			UUID uuid = UUID.fromString(resultSet.getString("uuid"));
			return new Agent(uuid, null, login);
		}

		return null;
	}

	/**
	 * Get the instance of the manager
	 * 
	 * @return the manager's instance
	 * 
	 * @see DBManager
	 */
	public static DBManager getInstance() {
		return instance;
	}

//	public static void main(String[] args) throws UnknownHostException, IOException {
//		Socket socket = new Socket("10.32.44.113", 9000);
//		
//		DataInputStream in = new DataInputStream(socket.getInputStream());
//		int port = in.readInt();
//		
//		socket.close();
//		socket = new Socket("10.32.44.113", port);
//		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//		out.writeUTF("Coucou!");
//	}

	@Override
	public void onAgentLogin(Agent agent) {
		try {
			addUser(agent, null);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onAgentLogout(Agent agent) {
	}

	@Override
	public void onSelfLogin(UUID uuid, String name) {
	}

	@Override
	public void onSelfLogout() {
		try {
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageReceived(Message message) {
		try {
			addMessage(message);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageSent(Message message) {
		try {
			addMessage(message);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws SQLException {
		DBManager manager = DBManager.instance;

		Agent a = new Agent(UUID.fromString("1544080c-8643-41cb-a2be-2962ce842b7a"), null, "a");
		Agent b = new Agent(UUID.fromString("1544080c-8643-41cb-a2be-2962ce842b8a"), null, "b");
		Agent c = new Agent(UUID.fromString("154542c-8643-41cb-a2be-2962ce842b7a"), null, "c");
		Agent d = new Agent(UUID.fromString("83574f09-3a2b-4778-96ec-3c74c69476cf"), null, "d");
		Agent e = new Agent(UUID.fromString("1544080c-8643-41cb-a2be-2962ce843b7a"), null, "e");

		ListenerManager.getInstance().fireAgentLogin(a);
		ListenerManager.getInstance().fireAgentLogin(b);
		ListenerManager.getInstance().fireAgentLogin(c);
		ListenerManager.getInstance().fireAgentLogin(d);
		ListenerManager.getInstance().fireAgentLogin(e);

		List<? extends Message> messages = manager.requestMessages(a);

		messages.forEach(t -> System.out.println(t.toString()));

		System.out.println();

		Message message = manager.requestMessage(a);
		System.out.println(message.toString());
	}

}
