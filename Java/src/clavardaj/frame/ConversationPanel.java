package clavardaj.frame;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

//import clavardaj.controller.DBManager;
import clavardaj.controller.ListenerManager;
import clavardaj.controller.UserManager;
//import clavardaj.controller.UserManager;
import clavardaj.controller.listener.ConversationChangeListener;
import clavardaj.model.Message;
import clavardaj.model.TextMessage;

public class ConversationPanel extends JPanel implements ConversationChangeListener {

	private JLabel name;
	private UUID uuid;
	private List<Message> messages;
	private JPanel messagesPanel;

	/**
	 * 
	 */
	private static final long serialVersionUID = 7900691014998310071L;

	public ConversationPanel() {
		ListenerManager.getInstance().addConversationChangeListener(this);

		this.name = new JLabel("");
		this.messages = new ArrayList<>();
		this.messagesPanel = buildMessagesPanel();

		this.add(name);
		this.add(messagesPanel);

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	}

	private JPanel buildMessagesPanel() {
		JPanel panel = new JPanel();
		panel.add(Box.createVerticalGlue());
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createLineBorder(Color.black));

		return panel;
	}

	private void updateMessagesPanel() {
		messages.clear();
		// messages =
		// DBManager.getInstance().requestMessages(UserManager.getInstance().getAgentByUuid(this.uuid));
		UUID uuid0 = UserManager.getInstance().getAgentList().get(0).getUuid();
		UUID uuid1 = UserManager.getInstance().getAgentList().get(1).getUuid();
		messages.add(new TextMessage("Hey bébou !", uuid0, uuid1, null));
		messages.add(new TextMessage("Hey cube", uuid1, uuid0, null));
		messages.add(new TextMessage("Comment ça va ?", uuid0, uuid1, null));
		messages.add(new TextMessage("Moi ça va bien en tout cas", uuid0, uuid1, null));

		for (Message message : messages) {
			messagesPanel.add(new MessagePanel(message));
		}
	}

	@Override
	public void onContactSelection(UUID uuid) {
		this.uuid = uuid;
		name.setText(UserManager.getInstance().getAgentByUuid(uuid).getName());
		updateMessagesPanel();
	}

}
