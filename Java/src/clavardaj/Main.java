package clavardaj;

import clavardaj.controller.ListenerManager;
import clavardaj.controller.UserManager;
import clavardaj.frame.MainFrame;

public class Main {
	
	public static void main(String[] args) {
		MainFrame frame = new MainFrame();
		frame.initFrame();
		ListenerManager.getInstance().addLoginListener(UserManager.getInstance());
	}
	
}
