package clavardaj.controller.listener;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface MessageToTransferListener {

	void onMessageToSend(DataOutputStream stream, String string);
	
	void onMessageToReceive(DataInputStream stream);
	
}
