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
import javax.swing.JTextArea;

import clavardaj.controller.UserManager;
import clavardaj.model.FileMessage;
import clavardaj.model.Message;
import clavardaj.model.TextMessage;

public class MessagePanel extends JPanel {

	private static final long serialVersionUID = -9060001645456922115L;

	public MessagePanel(Message message) {
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		String line = "";
		JTextArea messageBox;

		if (message.getSender().equals(UserManager.getInstance().getCurrentAgent().getUuid())) {
			if (message instanceof TextMessage) {
				line = new String(message.getContent());
			} else if (message instanceof FileMessage) {
				line = "Vous avez reçu un fichier : " + ((FileMessage) message).getFileName();
			}

			messageBox = createMessageBox(line);
//			this.add(Box.createHorizontalGlue());
			this.add(messageBox);
		} else {
			if (message instanceof TextMessage) {
				line = new String(message.getContent());
			} else if (message instanceof FileMessage) {
				line = "Vous avez envoyé un fichier : " + ((FileMessage) message).getFileName();
			}

			messageBox = createMessageBox(line);
			this.add(messageBox);
//			this.add(Box.createHorizontalGlue());
		}

		LocalDateTime date = message.getDate();
		if (Duration.between(date, LocalDateTime.now()).toDays() < 1) {
			this.add(new JLabel(date.format(DateTimeFormatter.ofPattern("   HH : mm"))));
		} else {
			this.add(new JLabel(date.format(DateTimeFormatter.ofPattern("   dd LLL uuuu, HH : mm"))));
		}
		
		
	}

	private JTextArea createMessageBox(String line) {
		// ATTENTION, ne pas utiliser getPreferredSize !!!!!
		// Augmente plus on le remplit !
		JTextArea messageBox = new JTextArea(line, 0, 50);
		System.out.println(messageBox.getFontMetrics(messageBox.getFont()).stringWidth(line));
		System.out.println(messageBox.getPreferredSize().width);
		System.out.println(messageBox.getPreferredScrollableViewportSize().width);
		System.out.println(messageBox.getFontMetrics(messageBox.getFont()).stringWidth(line)
				/ (double) messageBox.getPreferredScrollableViewportSize().width);
		System.out.println();

		messageBox.setRows((int) Math.ceil((double) messageBox.getFontMetrics(messageBox.getFont()).stringWidth(line)
				/ (double) messageBox.getPreferredScrollableViewportSize().width));
		messageBox.setLineWrap(true);
		messageBox.setWrapStyleWord(true);
		messageBox.setBackground(Color.pink);
		messageBox.setBorder(BorderFactory.createLineBorder(Color.black));
		messageBox.setOpaque(true);
		messageBox.setEditable(false);
		messageBox.setMaximumSize(messageBox.getPreferredSize());

//		System.out.println(messageBox.getFontMetrics(messageBox.getFont()).stringWidth("m"));

		return messageBox;
	}

}
