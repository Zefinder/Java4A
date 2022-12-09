package clavardaj.model.packet.receive;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import clavardaj.controller.ListenerManager;
import clavardaj.controller.UserManager;
import clavardaj.model.Agent;

public class PacketRcvLogout implements PacketToReceive {

	private UUID uuid;

	@Override
	public void initFromStream(DataInputStream stream) throws IOException {
		uuid = UUID.fromString(stream.readUTF());
	}

	@Override
	public void processPacket() {
		Agent distAgent = UserManager.getInstance().getAgentByUuid(uuid);
		ListenerManager.getInstance().fireAgentLogout(distAgent);
	}
	
	@Override
	public String toString() {
		return "[PacketRcvLogout]: uuid = " + uuid.toString();
	}

}
