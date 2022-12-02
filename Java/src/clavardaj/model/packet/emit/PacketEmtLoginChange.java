package clavardaj.model.packet.emit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import clavardaj.model.Agent;

public class PacketEmtLoginChange implements PacketToEmit {

	private UUID uuid;
	private InetAddress ip;
	private int port;
	private String name, newName;

	public PacketEmtLoginChange(Agent sender, String newName) {
		this.uuid = sender.getUuid();
		this.ip = sender.getIp();
		this.port = sender.getPort();
		this.name = sender.getName();
		this.newName = newName;
	}

	@Override
	public void sendPacket(DataOutputStream stream) throws IOException {
		stream.writeUTF(this.uuid.toString());
		stream.writeUTF(this.ip.toString());
		stream.writeInt(this.port);
		stream.writeUTF(this.name);
		stream.writeUTF(this.newName);
	}

}
