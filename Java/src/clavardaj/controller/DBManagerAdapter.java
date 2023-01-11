package clavardaj.controller;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import clavardaj.controller.database.DBManager;
import clavardaj.controller.database.InitializationException;
import clavardaj.controller.database.MySQLManager;
import clavardaj.controller.listener.LoginListener;
import clavardaj.controller.listener.MessageListener;
import clavardaj.model.Agent;
import clavardaj.model.Message;

/**
 * <p>
 * This manager is in charge of the database. Most of its methods are to use
 * directly without passing with a listener as they are situational
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
 * This manager is implemented as an adapter. It uses the {@link DBManager}
 * abstract class to use multiple types of databases
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
 * @see DBManager
 * @see MySQLManager
 * 
 * @since 1.0.0
 *
 */
public class DBManagerAdapter implements LoginListener, MessageListener {

	private static final DBManagerAdapter instance = new DBManagerAdapter();
	private DBManager dbManager;
	private boolean initOk = false;

	private DBManagerAdapter() {
		ListenerManager.getInstance().addLoginListener(this);
		ListenerManager.getInstance().addMessageListener(this);
	}

	private void setDatabaseManager(DBManager manager) {
		dbManager = manager;
	}

	private void addMessage(Message message) throws SQLException {
		dbManager.addMessage(message);
	}

	public boolean init() throws InitializationException {
		DBLoginsDialog dialog = new DBLoginsDialog();
		dialog.showFrame();

		if (dbManager == null)
			throw new InitializationException("La base de données n'a pas été correctement initialisée !");

		return true;
	}

	/**
	 * Adds a user in the user's table
	 * 
	 * @param agent  the agent to put in the table
	 * @param passwd the password of the agent if it is the main agent
	 * @throws SQLException if a database access error occurs
	 */
	private void addUser(Agent agent, String passwd) throws SQLException {
		dbManager.addUser(agent.getUuid(), agent.getName(), passwd);
	}

	/**
	 * Adds a user in the user's table
	 * 
	 * @param uuid   the agent's UUID to put in the table
	 * @param passwd the password of the agent if it is the main agent
	 * @throws SQLException if a database access error occurs
	 */
	private void addUser(UUID uuid, String name, String passwd) throws SQLException {
		dbManager.addUser(uuid, name, passwd);
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
		return dbManager.requestMessages(agent.getUuid());
	}

	/**
	 * Select the last message sent to or by the specified agent
	 * 
	 * @param agent the agent to get the messages from
	 * @return the last message sent by the agent
	 * @throws SQLException if a database access error occurs
	 */
	public Message requestMessage(Agent agent) throws SQLException {
		return dbManager.requestMessage(agent.getUuid());
	}

	/**
	 * Check if the login and password match to any line of the user table.
	 * 
	 * @param login  the agent's login
	 * @param passwd the agent's password
	 * @return the agent that matches (or null if none matches)
	 * @throws SQLException
	 * @throws InitializationException
	 */
	public Agent checkUser(String login, String passwd) throws SQLException, InitializationException {
		if (!initOk)
			initOk = init();

		return dbManager.checkUser(login, passwd);
	}

	/**
	 * Get the instance of the manager
	 * 
	 * @return the manager's instance
	 * 
	 * @see DBManagerAdapter
	 */
	public static DBManagerAdapter getInstance() {
		return instance;
	}

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
	public void onSelfLogin(UUID uuid, String name, String password) {
		try {
			addUser(uuid, name, password);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSelfLogout() {
		try {
			dbManager.closeConnection();
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

	private class DBLoginsDialog extends JDialog {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3851902625266967815L;

		private JTextField tableInstance;
		private JTextField login;
		private JTextField password;
		private JComboBox<String> dbType;
		private JButton confirm;

		public DBLoginsDialog() {
			this.setTitle("Database login");
			this.setSize(500, 300);
			this.setResizable(false);
			this.setLocationRelativeTo(null);
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			this.setModal(true);

			this.getRootPane().setDefaultButton(confirm);
			this.getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
			this.getRootPane().getActionMap().put("send", new InfoAction());

			JPanel panel = buildPanel();

			this.add(panel);
			this.setVisible(false);
		}

		private JPanel buildInfoPanel() {
			JPanel panel = new JPanel();
			panel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(5, 5, 5, 5);

			// Définition des champs
			JLabel typeLabel = new JLabel("Base de données");
			JLabel instanceLabel = new JLabel("Instance");
			JLabel loginLabel = new JLabel("Login");
			JLabel passwordLabel = new JLabel("Mot de passe");

			dbType = new JComboBox<>(new String[] { "MySQL", "SQLite" });
			dbType.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
			dbType.getActionMap().put("send", new InfoAction());

			tableInstance = new JTextField("clavardaj", 20);
			tableInstance.addActionListener(new InfoAction());

			login = new JTextField("root", 20);
			login.addActionListener(new InfoAction());

			password = new JTextField(20);
			password.addActionListener(new InfoAction());

			// Placement
			c.gridx = 0;
			c.gridy = 0;
			panel.add(typeLabel, c);

			c.gridx = 1;
			c.gridy = 0;
			panel.add(dbType, c);

			c.gridx = 0;
			c.gridy = 1;
			panel.add(instanceLabel, c);

			c.gridx = 1;
			c.gridy = 1;
			panel.add(tableInstance, c);

			c.gridx = 0;
			c.gridy = 2;
			panel.add(loginLabel, c);

			c.gridx = 1;
			c.gridy = 2;
			panel.add(login, c);

			c.gridx = 0;
			c.gridy = 3;
			panel.add(passwordLabel, c);

			c.gridx = 1;
			c.gridy = 3;
			panel.add(password, c);

			return panel;
		}

		private JPanel buildPanel() {
			JPanel panel = new JPanel();
			panel.setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.anchor = GridBagConstraints.CENTER;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(10, 0, 10, 0);

			// Déclarations
			JLabel textLabel = new JLabel("Connexion à la base de données :");
			textLabel.setHorizontalAlignment(SwingConstants.CENTER);

			JPanel info = buildInfoPanel();

			confirm = new JButton("Se connecter");
			confirm.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "send");
			confirm.getActionMap().put("send", new InfoAction());
			confirm.addActionListener(new InfoAction());

			// Placement
			c.gridx = 0;
			c.gridy = 0;
			panel.add(textLabel, c);

			c.gridx = 0;
			c.gridy = 1;
			panel.add(info, c);

			c.gridx = 0;
			c.gridy = 2;
			panel.add(confirm, c);

			return panel;
		}

		public void showFrame() {
			this.setVisible(true);
		}

		private class InfoAction extends AbstractAction {

			/**
			 * 
			 */
			private static final long serialVersionUID = -8089730140984865384L;

			@Override
			public void actionPerformed(ActionEvent e) {
				DBManager dbManager;

				try {
					Class<?> clazz = Class.forName(
							String.format("clavardaj.controller.database.%sManager", dbType.getSelectedItem()));

					dbManager = (DBManager) clazz.getConstructor().newInstance();

				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException | NoSuchMethodException
						| SecurityException e1) {
					e1.printStackTrace();
					return;
				}

				try {
					dbManager.init(tableInstance.getText(), login.getText(), password.getText());
					setDatabaseManager(dbManager);
					dispose();
				} catch (SQLException e1) {
					JOptionPane.showMessageDialog(null, String.format("Erreur base de données : %s", e1.getMessage()),
							"Erreur !", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	public static void main(String[] args) throws InitializationException {
		instance.init();
	}

}
