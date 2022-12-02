package clavardaj.model.packet.receive;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import clavardaj.controller.ListenerManager;
import clavardaj.model.Agent;

public class PacketRcvLogout implements PacketToReceive {

	private UUID uuid;
	private InetAddress ip;
	private String name;

	@Override
	public void initFromStream(DataInputStream stream) throws IOException {
		uuid = UUID.fromString(stream.readUTF());
		this.ip = InetAddress.getByName(stream.readUTF());
		name = stream.readUTF();
	}

	@Override
	public void processPacket() {
		ListenerManager.getInstance().fireAgentLogout(new Agent(uuid, ip, name));
	}

}
