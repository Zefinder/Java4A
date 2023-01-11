package clavardaj.test;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import clavardaj.controller.DBManagerAdapter;
import clavardaj.controller.UserManager;
import clavardaj.controller.database.InitializationException;
import clavardaj.model.Agent;
import clavardaj.model.FileMessage;
import clavardaj.model.Message;
import clavardaj.model.TextMessage;

class DatabaseTest {

	private static Agent a, b;
	private static Message atob, btoa, file;
	private static DBManagerAdapter dbmanager;

	@BeforeAll
	public static void init() throws UnknownHostException, SQLException, InterruptedException, InitializationException {
		a = new Agent(UUID.randomUUID(), InetAddress.getLocalHost(), "a");
		b = new Agent(UUID.randomUUID(), InetAddress.getLocalHost(), "b");
		UserManager.getInstance().onAgentLogin(a);
		UserManager.getInstance().onAgentLogin(b);

		atob = new TextMessage("a -> b", a.getUuid(), b.getUuid());
		Thread.sleep(1000);
		btoa = new TextMessage("b -> a", b.getUuid(), a.getUuid());
		Thread.sleep(1000);
		file = new FileMessage("Test.txt", new byte[0], a.getUuid(), b.getUuid());

		dbmanager = DBManagerAdapter.getInstance();
		dbmanager.init();
	}

	@Test
	public void addUserTest() throws SQLException, UnknownHostException, InitializationException {
		dbmanager.onSelfLogin(a.getUuid(), a.getName(), "passwd");
		dbmanager.onAgentLogin(b);

		Agent retreivedA = dbmanager.checkUser("a", "passwd");
		Agent retreivedB = dbmanager.checkUser("b", "null");
		Agent nullAgent = dbmanager.checkUser("c", "null");

		assertEquals(a.getUuid(), retreivedA.getUuid());
		assertEquals(b.getUuid(), retreivedB.getUuid());
		assertNull(nullAgent);
	}

	@Test
	public void addMessageTest() throws SQLException {
		dbmanager.onMessageSent(atob);
		dbmanager.onMessageReceived(btoa);
		dbmanager.onMessageSent(file);

		Message message = dbmanager.requestMessage(a);
		Message message2 = dbmanager.requestMessage(b);

		if (message instanceof FileMessage && message2 instanceof FileMessage) {
			assertEquals(((FileMessage) message).getFileName(), ((FileMessage) message2).getFileName());
			assertEquals(((FileMessage) file).getFileName(), ((FileMessage) message).getFileName());
		} else
			fail("Last message concerning both agents isn't a FileMessage " + message);

		List<? extends Message> messagesA = dbmanager.requestMessages(a);
		List<? extends Message> messagesB = dbmanager.requestMessages(b);

		for (int i = 1; i <= 3; i++) {
			message = messagesA.get(messagesA.size() - i);
			message2 = messagesB.get(messagesB.size() - i);

			if (message instanceof TextMessage && message2 instanceof TextMessage) {
				if (message.getSender().equals(a.getUuid())) {
					assertEquals(((TextMessage) message).getStringContent(),
							((TextMessage) message2).getStringContent());
					assertEquals(((TextMessage) atob).getStringContent(), ((TextMessage) message).getStringContent());
				} else {
					assertEquals(((TextMessage) message).getStringContent(),
							((TextMessage) message2).getStringContent());
					assertEquals(((TextMessage) btoa).getStringContent(), ((TextMessage) message).getStringContent());
				}
			} else if (message instanceof FileMessage && message2 instanceof FileMessage) {
				assertEquals(((FileMessage) message).getFileName(), ((FileMessage) message2).getFileName());
				assertEquals(((FileMessage) file).getFileName(), ((FileMessage) message).getFileName());
			} else
				fail(String.format("Inconsistency on messages: %s and %s", message, message2));
		}
	}

	@AfterAll
	public static void clean() throws SQLException {
		final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());

		// Connection à la DB
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/clavardaj", "root", "");
		java.sql.Statement statement = connection.createStatement();

		statement.execute(String.format("DELETE FROM `user` WHERE `user`.`uuid` = '%s';", a.getUuid()));
		statement.execute(String.format("DELETE FROM `user` WHERE `user`.`uuid` = '%s';", b.getUuid()));
		statement.execute(String.format(
				"DELETE FROM `message` WHERE `message`.`userSend` = '%s' AND `message`.`userRcv` = '%s' AND `message`.`date` = '%s';",
				a.getUuid(), b.getUuid(), atob.getDate().format(formatter)));
		statement.execute(String.format(
				"DELETE FROM `message` WHERE `message`.`userSend` = '%s' AND `message`.`userRcv` = '%s' AND `message`.`date` = '%s';",
				b.getUuid(), a.getUuid(), btoa.getDate().format(formatter)));
		statement.execute(String.format(
				"DELETE FROM `message` WHERE `message`.`userSend` = '%s' AND `message`.`userRcv` = '%s' AND `message`.`date` = '%s';",
				a.getUuid(), b.getUuid(), file.getDate().format(formatter)));

		statement.close();
		connection.close();
	}

}
