package clavardaj.model.packet.emit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import clavardaj.controller.UserManager;

public class PacketEmtMessage implements PacketToEmit {

	private UUID uuid;

	public PacketEmtMessage() {
		// On n'envoie pas l'UUID de celui à qui on doit envoyer mais sa propre UUID ! 
		this.uuid = UserManager.getInstance().getCurrentAgent().getUuid();
	}

	@Override
	public void sendPacket(DataOutputStream stream) throws IOException {
		stream.writeUTF(this.uuid.toString());
	}

}
