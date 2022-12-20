package clavardaj.test;

import static org.junit.Assert.assertTrue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import clavardaj.controller.ListenerManager;
import clavardaj.controller.PacketManager;
import clavardaj.controller.UserManager;
import clavardaj.controller.listener.ConversationListener;
import clavardaj.controller.listener.LoginChangeListener;
import clavardaj.controller.listener.LoginListener;
import clavardaj.controller.listener.MessageToTransferListener;
import clavardaj.model.Agent;
import clavardaj.model.Message;
import clavardaj.model.packet.emit.PacketEmtCloseConversation;
import clavardaj.model.packet.emit.PacketEmtLogin;
import clavardaj.model.packet.emit.PacketEmtLoginChange;
import clavardaj.model.packet.emit.PacketEmtLogout;
import clavardaj.model.packet.emit.PacketEmtMessage;
import clavardaj.model.packet.emit.PacketEmtOpenConversation;
import clavardaj.model.packet.emit.PacketToEmit;

/**
 * Tests for sending and receiving packets and verifying that everything is
 * correctly sent
 * 
 * @author Adrien Jakubiak
 */
@TestMethodOrder(OrderAnnotation.class)
class PacketTests implements LoginListener, LoginChangeListener, MessageToTransferListener, ConversationListener {

	private static DataOutputStream out;
	private static Socket socket;

	
	private static PacketManager p;
	private static UserManager u;
	private PacketToEmit packet;

	private static Agent agent;

	private static final PacketTests instance = new PacketTests();

	private static boolean loginOk, loginChangeOk, openConversationOk, closeConversationOk, sendMessageOk, logoutOk;

	@BeforeAll
	public static void init() throws InterruptedException, UnknownHostException {
		p = PacketManager.getInstance();
		u = UserManager.getInstance();

		ListenerManager.getInstance().addLoginListener(instance);
		ListenerManager.getInstance().addLoginChangeListener(instance);
		ListenerManager.getInstance().addMessageToTransferListener(instance);
		ListenerManager.getInstance().addConversationListener(instance);

		ListenerManager.getInstance().fireSelfLogin(UUID.randomUUID(), "Host");

		Thread.sleep(200);

		try {
			socket = new Socket("localhost", 1234);
			DataInputStream in = new DataInputStream(socket.getInputStream());

			int newPort = in.readInt();

			socket.close();
			socket = new Socket("localhost", newPort);
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		PacketToEmit packet = new PacketEmtLogin(u.getCurrentAgent().getUuid(), InetAddress.getByName("127.0.0.1"),
				u.getCurrentAgent().getName());
		p.sendPacket(out, packet);

	}

	@Test
	@Order(1)
	public void testPacketLogin() throws UnknownHostException, InterruptedException {
		agent = new Agent(UUID.randomUUID(), InetAddress.getByName("127.0.0.1"), "Test");

		packet = new PacketEmtLogin(agent.getUuid(), agent.getIp(), agent.getName());
		p.sendPacket(out, packet);
		Thread.sleep(50);
		assertTrue(loginOk);
	}

	@Test
	@Order(2)
	public void testPacketChangeLogin() throws InterruptedException {
		packet = new PacketEmtLoginChange("Test Change");

		p.sendPacket(out, packet);
		Thread.sleep(50);
		assertTrue(loginChangeOk);
	}

	@Test
	@Order(6)
	public void testPacketLogout() throws InterruptedException {
		packet = new PacketEmtLogout();

		p.sendPacket(out, packet);
		Thread.sleep(50);
		assertTrue(logoutOk);
	}

	@Test
	@Order(3)
	public void testPacketOpenConversation() throws InterruptedException {
		packet = new PacketEmtOpenConversation(agent);

		p.sendPacket(out, packet);
		Thread.sleep(50);
		assertTrue(openConversationOk);
	}

	@Test
	@Order(5)
	public void testPacketCloseConversation() throws InterruptedException {
		packet = new PacketEmtCloseConversation();

		p.sendPacket(out, packet);
		Thread.sleep(50);
		assertTrue(closeConversationOk);
	}

	@Test
	@Order(4)
	public void testPacketMessage() throws InterruptedException {
		packet = new PacketEmtMessage(false);

		p.sendPacket(out, packet);
		Thread.sleep(50);
		assertTrue(sendMessageOk);
	}

	@Override
	public void onConversationOpening(Agent agent, int localPort) {
	}

	@Override
	public void onConversationClosing(Agent agent) {
	}

	@Override
	public void onConversationOpened(Agent agent) {
		openConversationOk = agent.equals(u.getCurrentAgent());
	}

	@Override
	public void onConversationClosed(Agent agent) {
		closeConversationOk = agent.equals(u.getCurrentAgent());
	}

	@Override
	public void onMessageToSend(Agent agent, Message message) {
		
	}

	@Override
	public void onMessageToReceive(Agent agent, boolean isFile) {
		sendMessageOk = agent.equals(u.getCurrentAgent()) && !isFile;
	}

	@Override
	public void onAgentLoginChange(Agent agent, String newLogin) {
		loginChangeOk = newLogin.equals("Test Change");
	}

	@Override
	public void onSelfLoginChange(String newLogin) {

	}

	@Override
	public void onAgentLogin(Agent agent) {
		if (!agent.getName().equals("Host"))
			loginOk = agent.equals(PacketTests.agent);
	}

	@Override
	public void onAgentLogout(Agent agent) {
		logoutOk = agent.equals(u.getCurrentAgent());
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void onSelfLogin(UUID uuid, String name) {

	}

	@Override
	public void onSelfLogout() {

	}
}
