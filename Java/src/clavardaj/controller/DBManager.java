package clavardaj.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBManager {

	private static final DBManager instance = new DBManager();

	private DBManager() {
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
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/clavardaj", "root", "");
		java.sql.Statement statement = connection.createStatement();

		// Vérification de l'existance des tables (sinon création)
		// CREATE TABLE `clavardaj`.`user` ( `uuid` VARCHAR(36) NOT NULL , `login`
		// VARCHAR(20) NOT NULL , `passwd` VARCHAR(32) NULL , PRIMARY KEY (`uuid`(36)));
		// CREATE TABLE `clavardaj`.`message` ( `userSend` VARCHAR(36) NOT NULL ,
		// `userRcv` VARCHAR(36) NOT NULL , `date` DATE NOT NULL , `content`
		// VARCHAR(2048) NOT NULL , PRIMARY KEY (`userSend`, `userRcv`, `date`));
		// SELECT DISTINCT `TABLE_NAME` FROM `INFORMATION_SCHEMA`. `COLUMNS` WHERE
		// `table_schema` = 'clavardaj';
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
					"CREATE TABLE `clavardaj`.`message` ( `userSend` VARCHAR(36) NOT NULL , `userRcv` VARCHAR(36) NOT NULL , `date` DATE NOT NULL , `content` VARCHAR(2048) NOT NULL , PRIMARY KEY (`userSend`, `userRcv`, `date`));");
		}
	}

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

	public static void main(String[] args) {
		DBManager manager = DBManager.instance;
	}
}
