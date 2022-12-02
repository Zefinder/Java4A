package clavardaj.model.packet.receive;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

import clavardaj.controller.ListenerManager;
import clavardaj.model.Agent;

public class PacketRcvLogin implements PacketToReceive {

	private String name;

	@Override
	public void initFromStream(DataInputStream stream) throws IOException {
		this.name = stream.readUTF();
	}

	@Override
	public void processPacket() {
		UUID uuid = UUID.randomUUID();
		Agent agent = new Agent(uuid, name);
		ListenerManager.getInstance().fireAgentLogin(agent);
	}

	@Override
	public String toString() {
		return String.format("[PacketRcvLogin]: name = %s", name);
	}

}
