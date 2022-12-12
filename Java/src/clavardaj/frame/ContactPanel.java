package clavardaj.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import clavardaj.controller.ListenerManager;
import clavardaj.controller.listener.LoginChangeListener;
import clavardaj.controller.listener.LoginListener;
import clavardaj.model.Agent;

public class ContactPanel extends JPanel implements LoginChangeListener, LoginListener, MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5977339537047103792L;

	enum State {
		CONNECTED, DISCONNECTED;
	}
	
	private UUID uuid;

	private String login;

	// Pour plus tard afficher une image sur une personne
	private ImageIcon icon;

	// Pour savoir s'il est connecté ou non
	private State state;

	private JLabel labelLogin;

	public ContactPanel(String login, State state) {
		ListenerManager.getInstance().addLoginChangeListener(this);
		ListenerManager.getInstance().addLoginListener(this);
		this.addMouseListener(this);

		this.login = login;
		this.state = state;

		this.setLayout(new GridBagLayout());

		labelLogin = new JLabel(login);

		if (state == State.DISCONNECTED)
			labelLogin.setForeground(new Color(150, 150, 150));
		
		this.add(labelLogin);

		this.setPreferredSize(new Dimension(200, 50));
		this.setMaximumSize(getPreferredSize());
		this.setBackground(Color.WHITE);

		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}

	public String getLogin() {
		return login;
	}

	private void setLogin(String login) {
		this.login = login;
	}

	public ImageIcon getIcon() {
		return icon;
	}

//	public void setIcon(ImageIcon icon) {
//		this.icon = icon;
//	}

	public State getState() {
		return state;
	}

	public void connect() {
		this.state = State.CONNECTED;
		labelLogin.setForeground(Color.BLACK);
	}

	public void disconnect() {
		this.state = State.DISCONNECTED;
		labelLogin.setForeground(new Color(150, 150, 150));
	}

	@Override
	public void onSelfLoginChange(String newLogin) {
		// On s'en fiche
	}

	@Override
	public void onAgentLoginChange(Agent agent, String newLogin) {
		if (login.equals(agent.getName()))
			setLogin(newLogin);
	}

	@Override
	public void onAgentLogin(Agent agent) {
		if (login.equals(agent.getName()))
			connect();

	}

	@Override
	public void onAgentLogout(Agent agent) {
		if (login.equals(agent.getName()))
			disconnect();
	}

	@Override
	public void onSelfLogin(UUID uuid, String name) {

	}

	@Override
	public void onSelfLogout() {

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		JOptionPane.showMessageDialog(null,
				String.format("C'est le moment où on ouvre la conversation avec %s, c'est ça ?", login), "Mmmmh",
				JOptionPane.INFORMATION_MESSAGE);
		ListenerManager.getInstance().fireContactSelection(uuid);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		this.setBackground(Color.LIGHT_GRAY);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		this.setBackground(Color.WHITE);

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

}
