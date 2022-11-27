package clavardaj.model.packet.emit;

import java.io.DataOutputStream;
import java.io.IOException;

public class PacketEmtLogin implements PacketToEmit {

	private String name;
	
	public PacketEmtLogin(String name) {
		this.name = name;
	}
	
	@Override
	public void sendPacket(DataOutputStream stream) throws IOException {
		stream.writeUTF(name);
	}

	@Override
	public String toString() {
		return String.format("[PacketEmtLogin]: name = %s", name);
	}
}
