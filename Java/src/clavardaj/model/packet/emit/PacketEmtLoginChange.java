package clavardaj.model.packet.emit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import clavardaj.model.Agent;

public class PacketEmtLoginChange implements PacketToEmit {

	private UUID uuid;
	private String newName;

	public PacketEmtLoginChange(Agent sender, String newName) {
		this.uuid = sender.getUuid();
		this.newName = newName;
	}

	@Override
	public void sendPacket(DataOutputStream stream) throws IOException {
		stream.writeUTF(this.uuid.toString());
		stream.writeUTF(this.newName);
	}

	@Override
	public String toString() {
		return String.format("[PacketEmtLoginChange]: uuid = %s, new namee = %s", uuid, newName);
	}

}
