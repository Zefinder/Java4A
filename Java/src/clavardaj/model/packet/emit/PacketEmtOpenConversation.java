package clavardaj.model.packet.emit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

import clavardaj.controller.ListenerManager;
import clavardaj.controller.PacketManager;
import clavardaj.controller.UserManager;
import clavardaj.model.Agent;

public class PacketEmtOpenConversation implements PacketToEmit {

	private UUID uuid;
	private int port;

	public PacketEmtOpenConversation(Agent agent) {
		// Pour ouvrir une conversation, on ne donne pas l'UUID de l'agent distant,
		// sinon il va essayer de s'ouvrir lui même. On donne la sienne !
		// Le paramètre est à enlever à la fin des tests !
		this.uuid = UserManager.getInstance().getCurrentAgent().getUuid();
		this.port = PacketManager.getInstance().getNextAvailablePort();

		new Thread(new Runnable() {
			
			@Override
			public void run() {
				ListenerManager.getInstance().fireConversationOpening(agent, port);				
			}
		}).start();
	}

	@Override
	public void sendPacket(DataOutputStream stream) throws IOException {
		stream.writeUTF(this.uuid.toString());
		stream.writeInt(this.port);
	}
	
	@Override
	public String toString() {
		return String.format("[PacketEmtOpenConversation]: uuid = %s, port = %d", uuid.toString(), port);
	}

}
