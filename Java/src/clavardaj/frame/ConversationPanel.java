package clavardaj.frame;

import java.awt.Color;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import clavardaj.controller.ListenerManager;
import clavardaj.controller.listener.ConversationChangeListener;

public class ConversationPanel extends JPanel implements ConversationChangeListener {

	JLabel name;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7900691014998310071L;

	public ConversationPanel() {
		name = new JLabel("NOM");
		this.add(name);
		this.setBorder(BorderFactory.createLineBorder(Color.black));
		ListenerManager.getInstance().addConversationChangeListener(this);
	}


	@Override
	public void onContactSelection(UUID uuid) {
		
//		name.setText(UserManager.getInstance().getAgentByUuid(uuid).getName());
		name.setText("test");
		
	}

}
