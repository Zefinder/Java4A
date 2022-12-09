package clavardaj.frame;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import clavardaj.frame.ContactPanel.State;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1108758077352467606L;

	private JTextField searchField;
	private ArrayList<ContactPanel> contacts;

	public MainFrame(String login) {
		this.setTitle("Clavardaj - " + login);
		this.setSize(1000, 700);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);

		JPanel panel = buildMainPanel();

		this.setContentPane(panel);

		this.setVisible(false);
	}

	private JPanel buildLeftHandPanel() {
		JPanel panel = new JPanel();

		searchField = new JTextField("Rechercher un contact");
		searchField.setPreferredSize(new Dimension(200, 30));
		searchField.setMaximumSize(searchField.getPreferredSize());

		searchField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (searchField.getText().isEmpty()) {
					searchField.setText("Rechercher un contact");
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				if (searchField.getText().equals("Rechercher un contact")) {
					searchField.setText("");
				}
			}
		});

		searchField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				contacts.get(0).setVisible(false);
			}
		});

		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		panel.add(buildSelfContactPanel());
		panel.add(searchField);
		panel.add(buildContactPanel());

		return panel;
	}

	private JPanel buildMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

		// Ajouter un JMenu
//		// Ajouter le panel des contacts et des conversations (JTabbedPane)
//		JPanel contactPanel = buildContactPanel();
//		// Ajouter le panel de l'utilisateur local
//		JPanel selfContactPanel = buildSelfContactPanel();
//		// Ajouter le panel de la conversation

		JPanel leftHandPanel = buildLeftHandPanel();
		JPanel conversationPanel = buildConversationPanel();
		
		JSeparator jSeparator = new JSeparator(SwingConstants.VERTICAL);
		jSeparator.setPreferredSize(new Dimension (10, this.getHeight()));
		jSeparator.setMaximumSize(jSeparator.getPreferredSize());
		
		// Création d'un espace horizontal non sécable
		panel.add(Box.createHorizontalStrut(10));
		panel.add(leftHandPanel);
		panel.add(Box.createHorizontalStrut(10));
		// Ajout d'une barre verticale
		panel.add(jSeparator);
		panel.add(conversationPanel);

		return panel;
	}

	private JPanel buildContactPanel() {
		JPanel panel = new JPanel();
		contacts = new ArrayList<>();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		ContactPanel cp1 = new ContactPanel("Bébou", State.CONNECTED);
		ContactPanel cp2 = new ContactPanel("Cube", State.DISCONNECTED);
		contacts.add(cp1);
		contacts.add(cp2);

		contacts.forEach(contact -> panel.add(contact));
		
		panel.add(Box.createVerticalGlue());
		return panel;

	}

	private JPanel buildSelfContactPanel() {
		JPanel panel = new SelfContactPanel();
		return panel;
	}

	private JPanel buildConversationPanel() {
		JPanel panel = new ConversationPanel();
		return panel;
	}

	public void showFrame() {
		this.setVisible(true);
	}

	public static void main(String[] args) {
		new MainFrame("Test").showFrame();
	}

}
