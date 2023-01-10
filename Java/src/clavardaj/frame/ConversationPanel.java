package clavardaj.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

//import clavardaj.controller.DBManager;
import clavardaj.controller.ListenerManager;
import clavardaj.controller.UserManager;
//import clavardaj.controller.UserManager;
import clavardaj.controller.listener.ConversationChangeListener;
import clavardaj.model.FileMessage;
import clavardaj.model.Message;
import clavardaj.model.TextMessage;

public class ConversationPanel extends JPanel implements ConversationChangeListener {

	private JLabel name;
	private JTextField messageField;
	private UUID uuid;
	private List<Message> messages; // les messages du panel
	private List<Message> messagesBebou, messagesCube; // listes uniquement pour les tests, pour éviter la bdd
	private JScrollPane scrollPane;
	private JPanel messagesPanel;

	private ListenerManager lmanager = ListenerManager.getInstance();
	private UserManager umanager = UserManager.getInstance();

	/**
	 * 
	 */
	private static final long serialVersionUID = 7900691014998310071L;

	public ConversationPanel() {
		ListenerManager.getInstance().addConversationChangeListener(this);

		this.messagesBebou = new ArrayList<>();
		this.messagesCube = new ArrayList<>();

		UUID uuidMe = UserManager.getInstance().getCurrentAgent().getUuid();
		UUID uuidBebs = UserManager.getInstance().getAgentList().get(0).getUuid();
		UUID uuidCube = UserManager.getInstance().getAgentList().get(1).getUuid();

		messagesBebou.add(new TextMessage("Hey bébou !", uuidMe, uuidBebs,
				LocalDateTime.parse("2018-05-26 12:14", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
		messagesBebou.add(new TextMessage("Hey cube", uuidBebs, uuidMe, LocalDateTime.now().minusDays(1)));
		messagesBebou.add(new TextMessage("Comment ça va ?", uuidMe, uuidBebs));
		messagesBebou.add(new TextMessage("Moi ça va bien en tout cas. Tiens regarde ça", uuidMe, uuidBebs));

		messagesBebou.add(new FileMessage("Triangle.png", null, uuidMe, uuidBebs));
		messagesBebou.add(new FileMessage("Wow.png", null, uuidBebs, uuidMe));

		messagesCube.add(new TextMessage("Cette fois-ci c'est moi qui commence", uuidCube, uuidMe,
				LocalDateTime.parse("2022-12-25 11:21", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
		messagesCube.add(new TextMessage("Désolé j'avais pas vu le message", uuidMe, uuidCube));

		this.name = new JLabel("");
		this.messages = new ArrayList<>();
		this.scrollPane = buildMessagesPanel();
		this.scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		this.messageField = new JTextField();
		messageField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = messageField.getText();
				if (!(text).equals("")) {
					TextMessage message = new TextMessage(messageField.getText(), umanager.getCurrentAgent().getUuid(),
							uuid);
					lmanager.fireMessageToSend(umanager.getCurrentAgent(), message);
					if (uuid == uuidBebs) {
						messagesBebou.add(message);
					} else if (uuid == uuidCube) {
						messagesCube.add(message);
					}
					messageField.setText("");
					addMessageToPanel(message);
					messagesPanel.add(Box.createVerticalStrut(5));
				}
			}
		});
		messageField.setMaximumSize(new Dimension(this.getMaximumSize().width, 30));

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		// TODO Ne pas afficher le field si on n'est pas sur un contact !
		this.add(name);
		this.add(scrollPane);
		this.add(messageField);
	}

	private JScrollPane buildMessagesPanel() {
		messagesPanel = new JPanel();
		messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.PAGE_AXIS));
		messagesPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		JScrollPane scroll = new JScrollPane(messagesPanel);
		messagesPanel.add(Box.createVerticalStrut(scroll.getViewport().getHeight()));

		return scroll;
	}

	private void rebuildMessagesPanel() {
		messagesPanel.removeAll();
		messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.PAGE_AXIS));
		messagesPanel.add(Box.createVerticalStrut((int) this.getParent().getPreferredSize().getHeight()));
	}

	private void updateMessagesPanel() {
		UUID uuidBebs = UserManager.getInstance().getAgentList().get(0).getUuid();
		UUID uuidCube = UserManager.getInstance().getAgentList().get(1).getUuid();
//		TODO à remplacer une fois que la db est en place
		// messages =
		// DBManager.getInstance().requestMessages(UserManager.getInstance().getAgentByUuid(this.uuid));
		if (uuid.equals(uuidBebs)) {
			messages = new ArrayList<>(messagesBebou);
		} else if (uuid.equals(uuidCube)) {
			messages = new ArrayList<>(messagesCube);
		}

		for (Message message : messages) {
			JPanel messagePanel = new MessagePanel(message);
			messagesPanel.add(messagePanel);
			messagesPanel.add(Box.createVerticalStrut(5));
		}

		updateUI();
	}
	
	private void addMessageToPanel(Message message) {
		messages.add(message);
		messagesPanel.add(new MessagePanel(message));
		updateUI();
	}

	@Override
	public void onContactSelection(UUID uuid, boolean active) {
		if (!uuid.equals(this.uuid)) {
			this.uuid = uuid;
			name.setText(UserManager.getInstance().getAgentByUuid(uuid).getName());
			rebuildMessagesPanel();
			updateMessagesPanel();
			messageField.setVisible(active);
		}
	}

}
