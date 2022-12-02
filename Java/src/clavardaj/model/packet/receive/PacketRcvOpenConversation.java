package clavardaj.model.packet.receive;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import clavardaj.controller.ListenerManager;
import clavardaj.model.Agent;

public class PacketRcvOpenConversation implements PacketToReceive {

	private UUID uuid;
	private InetAddress ip;
	private int port;
	private String name;

	@Override
	public void initFromStream(DataInputStream stream) throws IOException {
		this.uuid = UUID.fromString(stream.readUTF());
		this.ip = InetAddress.getByName(stream.readUTF());
		this.port = stream.readInt();
		this.name = stream.readUTF();
	}

	@Override
	public void processPacket() {
		Agent distAgent = new Agent(this.uuid, this.ip, this.port, this.name);
		ListenerManager.getInstance().fireConversationOpened(distAgent);
	}

}
