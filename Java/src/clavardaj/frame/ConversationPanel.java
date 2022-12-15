package clavardaj.frame;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

//import clavardaj.controller.DBManager;
import clavardaj.controller.ListenerManager;
//import clavardaj.controller.UserManager;
import clavardaj.controller.listener.ConversationChangeListener;
import clavardaj.model.Message;

public class ConversationPanel extends JPanel implements ConversationChangeListener {

	private JLabel name;
	private UUID uuid;
	private List<Message> messages;

	/**
	 * 
	 */
	private static final long serialVersionUID = 7900691014998310071L;

	public ConversationPanel() {
		this.name = new JLabel("");
		this.messages = new ArrayList<>();
		this.add(name);
		this.setBorder(BorderFactory.createLineBorder(Color.black));
		ListenerManager.getInstance().addConversationChangeListener(this);
	}

	private void buildMessagesPanel() {
		//messages = DBManager.getInstance().requestMessages(UserManager.getInstance().getAgentByUuid(this.uuid));
	}

	@Override
	public void onContactSelection(UUID uuid) {
		this.uuid = uuid;
//		name.setText(UserManager.getInstance().getAgentByUuid(uuid).getName());
		name.setText("test");
		buildMessagesPanel();
	}

}
