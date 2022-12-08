package clavardaj.model.packet.emit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import clavardaj.controller.ListenerManager;
import clavardaj.controller.PacketManager;
import clavardaj.model.Agent;

public class PacketEmtOpenConversation implements PacketToEmit {

	private UUID uuid;
	private int port;

	public PacketEmtOpenConversation(Agent agent) {
		this.uuid = agent.getUuid();
		this.port = PacketManager.getInstance().getNextAvailablePort();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ListenerManager.getInstance().fireConversationOpening(agent, port);				
			}
		}).start();
	}

	@Override
	public void sendPacket(DataOutputStream stream) throws IOException {
		stream.writeUTF(this.uuid.toString());
		stream.writeInt(this.port);

	}

}
