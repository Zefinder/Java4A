package clavardaj;

import clavardaj.controller.ListenerManager;
import clavardaj.controller.UserManager;
import clavardaj.frame.LoginFrame;

public class Main {
	
	public static final boolean DEBUG = true;
	
	public static void main(String[] args) {
		LoginFrame frame = new LoginFrame();
		frame.initFrame();
		ListenerManager.getInstance().addLoginListener(UserManager.getInstance());
	}
	
}
