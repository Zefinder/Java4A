package clavardaj.model.packet.receive;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import clavardaj.controller.ListenerManager;
import clavardaj.controller.UserManager;
import clavardaj.model.Agent;

public class PacketRcvLoginChange implements PacketToReceive {

	private UUID uuid;
	private String newName;

	@Override
	public void initFromStream(DataInputStream stream) throws IOException {
		this.uuid = UUID.fromString(stream.readUTF());
		this.newName = stream.readUTF();
	}

	@Override
	public void processPacket() {
		Agent distAgent = UserManager.getInstance().getAgentByUuid(uuid);
		ListenerManager.getInstance().fireAgentLoginChange(distAgent, newName);
	}

	@Override
	public String toString() {
		return String.format("[PacketRcvLoginChange]: uuid = %s, new namee = %s", uuid, newName);
	}
}
