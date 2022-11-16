package clavardaj.frame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3396247678085726366L;
	private JTextField login, password;

	public MainFrame() {
		this.setTitle("Clavardaj - Login");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setSize(500, 300);
		this.setLocationRelativeTo(null);

		JPanel framePanel = buildTotalPanel();

		this.setContentPane(new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 9047709955198397813L;
			Image backgroundImage = new ImageIcon(this.getClass().getResource("silence.png")).getImage();

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
		confirm.addActionListener(e -> JOptionPane.showOptionDialog(this, "I do not wish to be horny anymore !",
				"Silence wench !", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null,
				new String[] { "I just want to be happy" }, null));

		framePanel.add(confirm, c);
//		framePanel.setOpaque(false);

		framePanel.setBackground(new Color(0.8f, 0.8f, 0.8f, 0.25f));
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
		password = new JTextField(30);
		panel.add(password, c);

		return panel;
	}

	public void initFrame() {
		this.setVisible(true);
	}

}
