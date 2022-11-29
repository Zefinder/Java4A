package clavardaj.model.packet.emit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import clavardaj.model.Agent;

public class PacketEmtLogout implements PacketToEmit {

	private UUID uuid;
	private String name;

	public PacketEmtLogout(Agent agent) {
		this.uuid = agent.getUuid();
		this.name = agent.getName();
	}

	@Override
	public void sendPacket(DataOutputStream stream) throws IOException {
		stream.writeUTF(uuid.toString());
		stream.writeUTF(name);
	}

}
