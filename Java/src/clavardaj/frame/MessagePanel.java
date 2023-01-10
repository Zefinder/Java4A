package clavardaj.frame;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import clavardaj.controller.UserManager;
import clavardaj.model.FileMessage;
import clavardaj.model.Message;
import clavardaj.model.TextMessage;

public class MessagePanel extends JPanel {

	private static final long serialVersionUID = -9060001645456922115L;

	private UserManager umanager = UserManager.getInstance();

	public MessagePanel(Message message) {
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		JEditorPane text;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_START;

		// Nom expéditeur
		text = new JEditorPane();
		text.setBackground(Color.pink);
		text.setContentType("text/html");
		text.setEditable(false);
		if (message.getSender().equals(umanager.getCurrentAgent().getUuid())) {
			text.setText("<b>" + umanager.getCurrentAgent().getName() + "</b>");
		} else {
			text.setText("<b>" + umanager.getAgentByUuid(message.getSender()).getName() + "</b>");
		}
		c.gridx = 0;
		c.gridy = 0;
		this.add(text, c);

		// Date
		text = new JEditorPane();
		text.setBackground(Color.pink);
		text.setContentType("text/html");
		text.setEditable(false);
		if (isFromToday(message)) {
			text.setText("Aujourd'hui à " + message.getDate().format(DateTimeFormatter.ofPattern("HH:mm")));
		} else if (isFromYesterday(message)) {
			text.setText("Hier à" + message.getDate().format(DateTimeFormatter.ofPattern("HH:mm")));
		} else {
			text.setText(message.getDate().format(DateTimeFormatter.ofPattern("dd/LL/uuuu HH:mm")));
		}
		c.gridx = 1;
		c.gridy = 0;
		this.add(text, c);

		text = new JEditorPane();
		text.setBackground(Color.pink);
		text.setContentType("text/html");
		text.setEditable(false);
		if (message instanceof TextMessage) {
			text.setText(((TextMessage) message).getStringContent());
		} else if (message instanceof FileMessage) {
			text.setText("<u>" + ((FileMessage) message).getFileName() + "</u>");
			text.addMouseListener(new MouseListener() {
				
				@Override
				public void mouseReleased(MouseEvent e) {
				}
				
				@Override
				public void mousePressed(MouseEvent e) {
				}
				
				@Override
				public void mouseExited(MouseEvent e) {
				}
				
				@Override
				public void mouseEntered(MouseEvent e) {
				}
				
				@Override
				public void mouseClicked(MouseEvent e) {
					// TODO Auto-generated method stub
					System.out.println("cliiiiic");
				}
			});
		}
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.weightx = 1;
		this.add(text, c);
		text.setMinimumSize(this.getPreferredSize());

		this.setBorder(BorderFactory.createLineBorder(Color.RED));
		
//		text = createMessageBox(line);
	}

	private JTextArea createMessageBox(String line) {
		// ATTENTION, ne pas utiliser getPreferredSize !!!!!
		// Augmente plus on le remplit !
		JTextArea messageBox = new JTextArea(line, 0, 50);
//		System.out.println(messageBox.getFontMetrics(messageBox.getFont()).stringWidth(line));
//		System.out.println(messageBox.getPreferredSize().width);
//		System.out.println(messageBox.getPreferredScrollableViewportSize().width);
//		System.out.println(messageBox.getFontMetrics(messageBox.getFont()).stringWidth(line)
//				/ (double) messageBox.getPreferredScrollableViewportSize().width);
//		System.out.println();

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

	private boolean isFromToday(Message message) {
		LocalDateTime date = message.getDate();
		boolean ret = date.getDayOfYear() == LocalDateTime.now().getDayOfYear()
				? date.getYear() == LocalDateTime.now().getYear() ? true : false
				: false;
		return ret;
	}

	private boolean isFromYesterday(Message message) {
		LocalDateTime date = message.getDate();
		boolean ret = date.getDayOfYear() == LocalDateTime.now().minusDays(1).getDayOfYear()
				? date.getYear() == LocalDateTime.now().minusDays(1).getYear() ? true : false
				: false;
		return ret;
	}

}
