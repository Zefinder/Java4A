package clavardaj.model.packet.emit;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

public class PacketEmtLogin implements PacketToEmit {

	private UUID uuid;
	private InetAddress ip;
	private String name;

	public PacketEmtLogin(UUID uuid, InetAddress ip, String name) {
		this.uuid = uuid;
		this.ip = ip;
		this.name = name;
	}

	@Override
	public void sendPacket(DataOutputStream stream) throws IOException {
		stream.writeUTF(uuid.toString());
		stream.writeUTF(ip.getHostAddress());
		stream.writeUTF(name);
	}

	@Override
	public String toString() {
		return String.format("PacketEmtLogin[uuid=%s,name=%s,ip=%s]", uuid, name, ip.getHostAddress());
	}
}
