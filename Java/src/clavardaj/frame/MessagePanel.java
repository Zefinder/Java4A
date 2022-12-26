package clavardaj.frame;

import java.awt.Color;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import clavardaj.controller.UserManager;
import clavardaj.model.FileMessage;
import clavardaj.model.Message;
import clavardaj.model.TextMessage;

public class MessagePanel extends JPanel {

	private static final long serialVersionUID = -9060001645456922115L;

	public MessagePanel(Message message) {
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		String line = "";
		JLabel messageBox;

		if (message.getSender().equals(UserManager.getInstance().getCurrentAgent().getUuid())) {
			if (message instanceof TextMessage) {
				line = new String(message.getContent());
			} else if (message instanceof FileMessage) {
				line = "Vous avez reçu un fichier : " + ((FileMessage) message).getFileName();
			}

			messageBox = new JLabel(line);
			messageBox.setBackground(Color.pink);
			messageBox.setOpaque(true);
			messageBox.setBorder(BorderFactory.createLineBorder(Color.black));
			this.add(Box.createHorizontalGlue());
			this.add(messageBox);
		} else {
			if (message instanceof TextMessage) {
				line = new String(message.getContent());
			} else if (message instanceof FileMessage) {
				line = "Vous avez envoyé un fichier : " + ((FileMessage) message).getFileName();
			}

			messageBox = new JLabel(line);
			messageBox.setBackground(Color.pink);
			messageBox.setOpaque(true);
			messageBox.setBorder(BorderFactory.createLineBorder(Color.black));
			this.add(messageBox);
			this.add(Box.createHorizontalGlue());
		}

		LocalDateTime date = message.getDate();
		if (Duration.between(date, LocalDateTime.now()).toDays() < 1) {
			this.add(new JLabel(date.format(DateTimeFormatter.ofPattern("   HH : mm"))));
		} else {
			this.add(new JLabel(date.format(DateTimeFormatter.ofPattern("   dd LLL uuuu, HH : mm"))));
		}
	}

}
