package clavardaj.frame;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import clavardaj.controller.UserManager;
import clavardaj.model.Message;

public class MessagePanel extends JPanel {

	private static final long serialVersionUID = -9060001645456922115L;

	public MessagePanel(Message message) {
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

//		if (message.getSender().equals(UserManager.getInstance().getCurrentAgent().getUuid())) {
		if (message.getSender().equals(UserManager.getInstance().getAgentList().get(0).getUuid())) {
			this.add(Box.createHorizontalGlue());
			this.add(new JLabel(new String(message.getContent())));
		} else {
			this.add(new JLabel(new String(message.getContent())));
			this.add(Box.createHorizontalGlue());
		}
	}

}
