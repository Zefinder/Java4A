package clavardaj.model.packet.emit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import clavardaj.model.Agent;

public class PacketEmtMessage implements PacketToEmit {

	private UUID uuid;
	private InetAddress ip;
	private int port;
	private String name;

	public PacketEmtMessage(Agent sender) {
		this.uuid = sender.getUuid();
		this.ip = sender.getIp();
		this.port = sender.getPort();
		this.name = sender.getName();
	}

	@Override
	public void sendPacket(DataOutputStream stream) throws IOException {
		stream.writeUTF(this.uuid.toString());
		stream.writeUTF(this.ip.getHostAddress());
		stream.writeInt(this.port);
		stream.writeUTF(this.name);
	}

}
