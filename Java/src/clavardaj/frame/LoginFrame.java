package clavardaj.frame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import clavardaj.controller.DBManagerAdapter;
import clavardaj.controller.ListenerManager;
import clavardaj.controller.database.InitializationException;
import clavardaj.model.Agent;

public class LoginFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3396247678085726366L;
	private JTextField login;
	private JPasswordField password;

	public LoginFrame() {
		this.setTitle("Clavardaj - Login");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setSize(500, 500);
		this.setLocationRelativeTo(null);

		JPanel framePanel = buildTotalPanel();

		this.setContentPane(new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 9047709955198397813L;
			Image backgroundImage = new ImageIcon(this.getClass().getResource("background.png")).getImage();

			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
			}

		});

		this.setLayout(new GridBagLayout());
		this.add(framePanel);

		this.setVisible(false);
	}

	private JPanel buildTotalPanel() {
		JPanel framePanel = new JPanel();
		framePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(8, 5, 8, 0);

		JPanel loginPanel = buildLoginPanel();
		loginPanel.setOpaque(false);
		framePanel.add(loginPanel, c);

		c.gridy = 1;
		JButton confirm = new JButton("Login !");
		confirm.addActionListener(e -> {

			Agent agent = null;
			try {
				// TODO Hasher le password
				agent = DBManagerAdapter.getInstance().checkUser(login.getText(), new String(password.getPassword()));
			} catch (SQLException | InitializationException e1) {
				JOptionPane.showMessageDialog(this, String.format("Erreur base de données : %s", e1.getMessage()),
						"Erreur !", JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (agent == null) {
				int answer = JOptionPane.showOptionDialog(this, "Il semblerait que le compte n'existe pas... !",
						"Pas de compte !", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE, null,
						new String[] { "Je me suis trompé de mot de passe...", "Créer le compte !" }, null);

				if (answer == JOptionPane.NO_OPTION) {
					agent = new Agent(UUID.randomUUID(), null, login.getText());
				} else
					return;
			}

			ListenerManager.getInstance().fireSelfLogin(agent.getUuid(), agent.getName(),
					new String(password.getPassword()));

			MainFrame f = new MainFrame(agent.getName());
			f.showFrame();
			this.dispose();
		});

		framePanel.add(confirm, c);

		framePanel.setBackground(new Color(0.8f, 0.8f, 0.8f, 0.45f));
		return framePanel;
	}

	private JPanel buildLoginPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// 1st item
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(5, 0, 5, 10);

		JLabel loginLabel = new JLabel("Login :");
		panel.add(loginLabel, c);

		c.gridx = 1;
		login = new JTextField(30);
		panel.add(login, c);

		// 2nd item
		c.gridx = 0;
		c.gridy = 1;

		JLabel passwordLabel = new JLabel("Password :");
		panel.add(passwordLabel, c);

		c.gridx = 1;
		password = new JPasswordField(30);
		panel.add(password, c);

		return panel;
	}

	public void initFrame() {
		this.setVisible(true);
	}

	public static void main(String[] args) {
		new LoginFrame().initFrame();
	}

}
