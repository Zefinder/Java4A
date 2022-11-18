package clavardaj.controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import clavardaj.Main;
import clavardaj.model.packet.emit.PacketEmtLogin;
import clavardaj.model.packet.emit.PacketToEmit;
import clavardaj.model.packet.receive.PacketRcvLogin;
import clavardaj.model.packet.receive.PacketToReceive;

public class PacketManager implements Runnable {

	private static final PacketManager instance = new PacketManager();

	private final Map<Integer, Class<? extends PacketToReceive>> idToPacket = new HashMap<>();
	private final Map<Class<? extends PacketToEmit>, Integer> packetToId = new HashMap<>();
	private DataInputStream inputStream;
	private DataOutputStream outputStream;
	public static final BlockingQueue<PacketToReceive> packetsToHandle = new LinkedBlockingQueue<>();

	private PacketManager() {
		idToPacket.put(0, PacketRcvLogin.class);
		
		packetToId.put(PacketEmtLogin.class, 0);
		
		
	}

	public void connect() {
		// TODO Send UDP Packet to notify others you are here !
		// Setup your UDP Socket to listen in-going connections
		
	}

	@Override
	public void run() {
		while (true)
			try {
				int idPacket = inputStream.readInt();
				if (Main.DEBUG)
					System.out.println("Packet " + idPacket + " received !");

				PacketToReceive packet = readPacket(idPacket);
				packetsToHandle.add(packet);
			} catch (IOException | NoSuchMethodException | InvocationTargetException | InstantiationException
					| IllegalAccessException e) {
				e.printStackTrace();
				break;
			}
	}

	private PacketToReceive readPacket(int idPacket) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {
		Class<? extends PacketToReceive> clazz = idToPacket.get(idPacket);
		PacketToReceive packet = clazz.getDeclaredConstructor().newInstance();
		
		packet.initFromStream(inputStream);
		if (Main.DEBUG)
			System.out.println("Packet fully read: " + packet);
		
		return packet;
	}

	public void sendPacket(PacketToEmit packet) {
		try {
			outputStream.writeInt(packetToId.get(packet.getClass()));
			packet.sendPacket(outputStream);
			
			if (Main.DEBUG)
				System.out.println("Packet sent: " + packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static PacketManager getInstance() {
		return instance;
	}

}
