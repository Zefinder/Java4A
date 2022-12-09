package clavardaj.model.packet.receive;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import clavardaj.controller.ListenerManager;
import clavardaj.controller.UserManager;
import clavardaj.model.Agent;

public class PacketRcvOpenConversation implements PacketToReceive {

	private UUID uuid;
	private int port;

	@Override
	public void initFromStream(DataInputStream stream) throws IOException {
		this.uuid = UUID.fromString(stream.readUTF());
		this.port = stream.readInt();
	}

	@Override
	public void processPacket() {
		Agent distAgent = UserManager.getInstance().getAgentByUuid(uuid);
		distAgent.setPort(port);
		ListenerManager.getInstance().fireConversationOpened(distAgent);
	}

	@Override
	public String toString() {
		return String.format("[PacketRcvOpenConversation]: uuid = %s, port = %d", uuid.toString(), port);
	}
	
}
