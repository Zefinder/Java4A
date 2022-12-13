package clavardaj;

import java.io.File;

import clavardaj.controller.ListenerManager;
import clavardaj.controller.UserManager;
import clavardaj.frame.MainFrame;

public class Main {
	
	public static final boolean DEBUG = true;
	public static final File OUTPUT = new File("./output");
	
	public static void main(String[] args) {
		MainFrame frame = new MainFrame();
		frame.initFrame();
		ListenerManager.getInstance().addLoginListener(UserManager.getInstance());
	}
	
}
