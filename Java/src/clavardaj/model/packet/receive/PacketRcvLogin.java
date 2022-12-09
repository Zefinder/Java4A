package clavardaj.model.packet.receive;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import clavardaj.controller.ListenerManager;
import clavardaj.model.Agent;

public class PacketRcvLogin implements PacketToReceive {

	private UUID uuid;
	private InetAddress ip;
	private String name;

	@Override
	public void initFromStream(DataInputStream stream) throws IOException {
		this.uuid = UUID.fromString(stream.readUTF());
		this.ip = InetAddress.getByName(stream.readUTF());
		this.name = stream.readUTF();
	}

	@Override
	public void processPacket() {
		Agent agent = new Agent(uuid, ip, name);
		ListenerManager.getInstance().fireAgentLogin(agent);
	}

	@Override
	public String toString() {
		return String.format("PacketRcvLogin[uuid=%s,name=%s,ip=%s]", uuid, name, ip.getHostAddress());
	}

}
