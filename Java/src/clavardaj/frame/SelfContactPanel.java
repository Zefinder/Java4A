package clavardaj.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class SelfContactPanel extends JPanel {

	private static final long serialVersionUID = -3780257627603929253L;

	public SelfContactPanel() {
		
		
//		this.add(new JLabel(UserManager.getInstance().getCurrentAgent().getName()));
		this.add(new JLabel("BebouLocal"));
		
		this.setLayout(new GridBagLayout());
		
		this.setPreferredSize(new Dimension(200, 50));
		this.setMaximumSize(getPreferredSize());
		
		this.setBackground(Color.red);
		
		this.setBorder(BorderFactory.createLineBorder(Color.black));

	}

}
