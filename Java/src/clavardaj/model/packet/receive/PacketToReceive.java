package clavardaj.model.packet.receive;

import java.io.DataInputStream;
import java.io.IOException;

import clavardaj.model.packet.Packet;

public interface PacketToReceive extends Packet {
	
	void initFromStream(DataInputStream stream) throws IOException;
	void processPacket();
}
