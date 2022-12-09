package clavardaj.test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import clavardaj.controller.ListenerManager;
import clavardaj.controller.PacketManager;
import clavardaj.controller.ThreadManager;
import clavardaj.controller.UserManager;
import clavardaj.controller.listener.LoginListener;
import clavardaj.model.Agent;
import clavardaj.model.packet.emit.PacketToEmit;

public class TestPacketFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8193766432160460721L;

	private JTextField field;
	private JList<Agent> userList;

	private DataOutputStream out;

	private UserFrame uFrame;

	// Instanciation des managers
	ThreadManager tmanager = ThreadManager.getInstance();
	ListenerManager lmanagr = ListenerManager.getInstance();
	PacketManager pmanager = PacketManager.getInstance();
	// DBManager dmanager = DBManager.getInstance();
	UserManager umanager = UserManager.getInstance();

	public TestPacketFrame() {
		this.setTitle("Packet Frame");
		this.setSize(500, 300);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((int) ((dimension.getWidth() - getWidth()) / 4),
				(int) ((dimension.getHeight() - getHeight()) / 2));

		JPanel panel = buildPanel();
		this.add(panel);

		uFrame = new UserFrame();
		this.setVisible(false);
	}

	private JPanel buildPanel() {
		JPanel panel = new JPanel();
		JPanel buttonsPanel = buildButtonsPanel();
		panel.add(buttonsPanel, BorderLayout.CENTER);

		field = new JTextField(30);
		panel.add(field, BorderLayout.SOUTH);

		JButton connect = new JButton("Connect to distant user");
		connect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String ip = JOptionPane.showInputDialog(null, "Entrez l'IP de la machine distante", "Internet !",
						JOptionPane.INFORMATION_MESSAGE);

				try {
					System.out.println("Trying to connect to address " + ip);
					DatagramSocket socket = new DatagramSocket();
					DatagramPacket packet = new DatagramPacket("1234".getBytes(), 4, InetAddress.getByName(ip), 1233);
					socket.send(packet);
					socket.close();

				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		panel.add(connect, BorderLayout.SOUTH);

		return panel;
	}

	private JPanel buildButtonsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(5, 5));

		File packets = new File("./src/clavardaj/model/packet/emit");
		System.out.println(packets.getAbsolutePath());
		String[] files = packets.list();
		for (String file : files) {
			if (!file.equals("PacketToEmit.java")) {
				JButton button = new JButton(file.substring(0, file.length() - 5));
				button.addActionListener(new ButtonListener());

				panel.add(button);
			}
		}

		return panel;
	}

	public void showFrame() {
		this.setVisible(true);
		uFrame.showFrame();
	}

	private class UserFrame extends JFrame implements LoginListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2709924789268627185L;

		private DefaultListModel<Agent> model;

		public UserFrame() {
			this.setTitle("Users");
			this.setSize(500, 300);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation(3 * (int) ((dimension.getWidth() - getWidth()) / 4),
					(int) ((dimension.getHeight() - getHeight()) / 2));

			ListenerManager.getInstance().addLoginListener(this);

			JPanel panel = buildUserPanel();
			this.add(panel);

			this.setVisible(false);
		}

		private JPanel buildUserPanel() {
			JPanel panel = new JPanel();

			model = new DefaultListModel<>();
			userList = new JList<>(model);
			userList.setCellRenderer(new CellRenderer());

			panel.add(userList);

			return panel;
		}

		public void showFrame() {
			this.setVisible(true);
		}

		@Override
		public void onAgentLogin(Agent agent) {
			model.removeElement(agent);
			model.addElement(agent);
		}

		@Override
		public void onAgentLogout(Agent agent) {
			model.removeElement(agent);
		}

		@Override
		public void onSelfLogin(UUID uuid, String name) {
			// TODO
		}

		@Override
		public void onSelfLogout() {

		}
	}

	private class ButtonListener implements ActionListener {

		@SuppressWarnings("deprecation")
		@Override
		public void actionPerformed(ActionEvent e) {
			String buttonName = ((JButton) e.getSource()).getText();

			Agent agent = userList.getSelectedValue();
			if (agent == null) {
				userList.setSelectedIndex(0);
				agent = userList.getSelectedValue();
			}

			PacketToEmit packet = null;
			Class<?> packetClass = null;

			// On récupère la classe
			try {
				packetClass = Class.forName(String.format("clavardaj.model.packet.emit.%s", buttonName));
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}

			if (buttonName.equals("PacketEmtMessage")) {
				Agent agent1 = userList.getSelectedValue();
				if (agent1 == null) {
					userList.setSelectedIndex(0);
					agent1 = userList.getSelectedValue();
					if (agent1 == null) {
						System.err.println("There must be an agent to select to send this packet...");
						return;
					}
				}
				ListenerManager.getInstance().fireMessageToSend(agent1, field.getText());
			}

			// On prend le constructeur qui n'est pas celui hérité par Object et...
			for (Constructor<?> constructor : packetClass.getConstructors()) {
				if (constructor.getParameterCount() > 0) {
					List<Object> parameters = new ArrayList<>();

					for (Class<?> clazz : constructor.getParameterTypes()) {
						// On insère les paramètres au fur et à mesure !
						switch (clazz.getCanonicalName()) {
						case "java.lang.String":
							parameters.add(field.getText());
							break;

						case "java.time.LocalDateTime":
							parameters.add(LocalDateTime.now());
							break;

						case "java.util.UUID":
							if (agent == null) {
								parameters.add(UUID.randomUUID());
								break;
							}
							parameters.add(agent.getUuid());
							break;

						case "java.net.InetAddress":
							try {
								parameters.add(InetAddress.getByName("localhost"));
							} catch (UnknownHostException e2) {
								System.out.println("Problème get ip");
								e2.printStackTrace();
							}
							break;

						case "clavardaj.model.Agent":
							if (agent == null) {
								System.err.println("There must be an agent to select to send this packet...");
								return;
							}
							parameters.add(agent);
							break;
						default:
							break;
						}
					}

					// On utilise le constructeur avec les paramètres
					try {
						packet = (PacketToEmit) constructor.newInstance(parameters.toArray());
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e1) {
						e1.printStackTrace();
					}
				}
			}

			// Si jamais le packet n'avait pas d'argument, on l'instancie
			if (packet == null)
				try {
					packet = (PacketToEmit) packetClass.getDeclaredConstructor().newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
					e1.printStackTrace();
				}

			// Et on l'envoie !
			if (agent == null) {
				agent = umanager.getCurrentAgent();
			}

			try {
				if (agent.getIp().equals(InetAddress.getLocalHost()) || agent.getIp().equals(InetAddress.getLoopbackAddress()))
					pmanager.sendPacket(out, packet);
				else
					pmanager.sendPacket(agent.getIp(), packet);
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			}
		}

	}

	private class CellRenderer extends JLabel implements ListCellRenderer<Agent> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2085626785474925536L;

		public CellRenderer() {
			setOpaque(true);
			setHorizontalAlignment(CENTER);
			setVerticalAlignment(CENTER);
		}

		public Component getListCellRendererComponent(JList<? extends Agent> list, Agent displayItem, int index,
				boolean isSelected, boolean cellHasFocus) {

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			setText(displayItem.toString());
			return this;
		}

	}

	public static void main(String[] args) {
		TestPacketFrame frame = new TestPacketFrame();
		ListenerManager.getInstance().fireSelfLogin(UUID.randomUUID(), "Zefinder");

		Socket socket;
		try {
			socket = new Socket("localhost", 1234);
			DataInputStream in = new DataInputStream(socket.getInputStream());

			int newPort = in.readInt();

			socket.close();
			socket = new Socket("localhost", newPort);

			frame.out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		frame.showFrame();
	}

}
