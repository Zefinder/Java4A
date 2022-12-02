package clavardaj.model.packet.emit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import clavardaj.model.Agent;

public class PacketEmtLogout implements PacketToEmit {

	private UUID uuid;
	private InetAddress ip;
	private String name;

	public PacketEmtLogout(Agent agent) {
		this.uuid = agent.getUuid();
		this.ip = agent.getIp();
		this.name = agent.getName();
	}

	@Override
	public void sendPacket(DataOutputStream stream) throws IOException {
		stream.writeUTF(uuid.toString());
		stream.writeUTF(ip.getHostAddress());
		stream.writeUTF(name);
	}

}
