package clavardaj.model.packet.emit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import clavardaj.model.Agent;

public class PacketEmtMessage implements PacketToEmit {

	private UUID uuid;

	public PacketEmtMessage(Agent sender) {
		this.uuid = sender.getUuid();
	}

	@Override
	public void sendPacket(DataOutputStream stream) throws IOException {
		stream.writeUTF(this.uuid.toString());
	}

}
