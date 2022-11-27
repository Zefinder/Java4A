package clavardaj.test;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import clavardaj.controller.PacketManager;
import clavardaj.model.packet.emit.PacketEmtLogin;
import clavardaj.model.packet.emit.PacketToEmit;

public class TestPackets extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8193766432160460721L;
	
	private JTextField field;

	public TestPackets() {
		this.setTitle("Packet Frame");
		this.setSize(500, 300);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		
		JPanel panel = buildPanel();
		this.add(panel);
		
		this.setVisible(false);
	}
	
	private JPanel buildPanel() {
		JPanel panel = new JPanel();
		
		JPanel buttonsPanel = buildButtonsPanel();
		panel.add(buttonsPanel, BorderLayout.CENTER);
		
		field = new JTextField();
		panel.add(field, BorderLayout.SOUTH);
		
		return panel;
	}
	
	private JPanel buildButtonsPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(5, 5));
		
		File packets = new File("../model/packet/emit");
		String[] files = packets.list();
		for (String file : files) {
			if (!file.equals("PacketToEmit.java")) {
				JButton button = new JButton(file.substring(0, file.length()-5));
				button.addActionListener(new ButtonListener());
				
				panel.add(button);
			}
		}
		
		return panel;
	}
	
	public void showFrame() {
		this.setVisible(true);
	}

	private class ButtonListener implements ActionListener {
		
		private String name;

		@Override
		public void actionPerformed(ActionEvent e) {
			PacketToEmit packet = null;
			String buttonName = ((JButton) e.getSource()).getName();
			if(buttonName.equals("PacketEmtLogin")) {
				name = field.getText();
				packet = new PacketEmtLogin(name);
			}
			
			// Ajouter les autres paquets ici
			
			PacketManager.getInstance().sendPacket(null, packet);
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
