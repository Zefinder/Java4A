package clavardaj.model.packet.receive;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import clavardaj.controller.ListenerManager;
import clavardaj.model.Agent;

public class PacketRcvLogout implements PacketToReceive {

	private UUID uuid;
	private String name;

	@Override
	public void initFromStream(DataInputStream stream) throws IOException {
		uuid = UUID.fromString(stream.readUTF());
		name = stream.readUTF();
	}

	@Override
	public void processPacket() {
		ListenerManager.getInstance().fireAgentLogout(new Agent(uuid, name));
	}

}
