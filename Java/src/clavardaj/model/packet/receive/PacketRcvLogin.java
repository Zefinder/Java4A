package clavardaj.model.packet.receive;

import java.io.DataInputStream;
import java.io.IOException;

public class PacketRcvLogin implements PacketToReceive {

	private String name;

	@Override
	public void initFromStream(DataInputStream stream) throws IOException {
		name = stream.readUTF();

	}

	@Override
	public void processPacket() {
	}

	@Override
	public String toString() {
		return String.format("[PacketRcvLogin]: name = %s", name);
	}
	
}
