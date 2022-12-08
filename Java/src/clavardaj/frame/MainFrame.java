package clavardaj.frame;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import clavardaj.frame.ContactPanel.State;

public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1108758077352467606L;
	
	public MainFrame(String login) {
		this.setTitle("Clavardaj - " + login);
		this.setSize(1000, 700);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		
		JPanel panel = buildMainPanel();
		
		this.setContentPane(panel);
		
		this.setVisible(false);
	}
	
	private JPanel buildMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		
		// Ajouter un JMenu
		// Ajouter le panel des contacts et des conversations (JTabbedPane)
		JPanel contactPanel = buildContactPanel();
		// Ajouter le panel de la conversation
		
		
		// Création d'un espace horizontal non sécable
		panel.add(Box.createHorizontalStrut(10));
		panel.add(contactPanel);
		panel.add(Box.createHorizontalStrut(10));
		
		// Ajout d'une barre verticale
		panel.add(new JSeparator(SwingConstants.VERTICAL));
		
		return panel;
	}
	
	private JPanel buildContactPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		
		ContactPanel cp1 = new ContactPanel("Bébou", State.CONNECTED);
		ContactPanel cp2 = new ContactPanel("Cube", State.DISCONNECTED);
		
		panel.add(cp1);
		panel.add(cp2);
		panel.add(Box.createVerticalGlue());
		return panel;
		
	}
	
	public void showFrame() {
		this.setVisible(true);
	}
	
	public static void main(String[] args) {
		new MainFrame("Test").showFrame();
	}

}
