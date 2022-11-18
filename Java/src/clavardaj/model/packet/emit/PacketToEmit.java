package clavardaj.model.packet.emit;

import java.io.DataOutputStream;
import java.io.IOException;

import clavardaj.model.packet.Packet;

public interface PacketToEmit extends Packet {
	
	void sendPacket(DataOutputStream stream) throws IOException;
	
}
