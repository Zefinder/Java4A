package clavardaj.model.packet.receive;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import clavardaj.controller.ListenerManager;
import clavardaj.controller.UserManager;
import clavardaj.model.Agent;

public class PacketRcvMessage implements PacketToReceive {

	private UUID uuid;

	@Override
	public void initFromStream(DataInputStream stream) throws IOException {
		this.uuid = UUID.fromString(stream.readUTF());
	}

	@Override
	public void processPacket() {
		Agent sender = UserManager.getInstance().getAgentByUuid(uuid);
		ListenerManager.getInstance().fireMessageToReceive(sender);
	}

	@Override
	public String toString() {
		return "[PacketRcvMessage]: uuid = " + uuid.toString();
	}
}
