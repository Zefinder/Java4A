package clavardaj;

import clavardaj.controller.PacketManager;
import clavardaj.frame.MainFrame;

public class Main {
	
	public static final boolean DEBUG = true;
	
	public static void main(String[] args) {
		int port = (args.length != 0 ? Integer.valueOf(args[0]) : 1234);
		PacketManager.getInstance().setPort(port);		
		
		MainFrame frame = new MainFrame();
		frame.initFrame();
		
	}
	
}
