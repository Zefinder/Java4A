package clavardaj.model.packet.emit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import clavardaj.controller.UserManager;

public class PacketEmtCloseConversation implements PacketToEmit {

	private UUID uuid;
	public PacketEmtCloseConversation() {
		this.uuid = UserManager.getInstance().getCurrentAgent().getUuid();
		
	}

	@Override
	public void sendPacket(DataOutputStream stream) throws IOException {
		stream.writeUTF(uuid.toString());
	}
	
	@Override
	public String toString() {
		return "[PacketEmtCloseConversation]: uuid = " + uuid.toString();
	}

}
