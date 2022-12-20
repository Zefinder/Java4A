package clavardaj.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.swing.JFileChooser;

import clavardaj.Main;

public class TransferBigFileTest implements Runnable {

	private static final int chunkSize = 0x8000;

	public TransferBigFileTest() {

	}

	@Override
	public void run() {
		try {
			Socket socket = new Socket("localhost", 4444);
			System.out.println("[Client] - Connected to server !");
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			int answer = chooser.showOpenDialog(null);
			if (answer == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				writeFile(out, file);

				MessageDigest md5Digest = MessageDigest.getInstance("MD5");
				String checksum = getFileChecksum(md5Digest, Files.readAllBytes(file.toPath()));
				System.out.println(String.format("[Client] - File sent :     %s (size=%d), hash=%s", file.getName(),
						file.length(), checksum));
			}

			socket.close();

		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
		ServerSocket server = new ServerSocket(4444);
		new Thread(new TransferBigFileTest()).start();

		Socket socket = server.accept();
		System.out.println("[Server] - Client connected, ready to get the file...");
		server.close();

		DataInputStream in = new DataInputStream(socket.getInputStream());
		readFile(in, in.readInt(), in.readUTF());

	}

	private static void readFile(DataInputStream in, int len, String fileName)
			throws IOException, NoSuchAlgorithmException {
		byte[] buffer = new byte[chunkSize];

		File file = new File(Main.OUTPUT.getAbsolutePath() + File.separator + fileName);

		if (file.exists())
			file.delete();

		file.createNewFile();

		// On remplit le fichier
		FileOutputStream stream = new FileOutputStream(file);

		for (int size = len; size > 0; size -= chunkSize) {
			int byteRead = in.read(buffer, 0, Math.min(chunkSize, size));
			stream.write(buffer, 0, byteRead);
		}

		MessageDigest md5Digest = MessageDigest.getInstance("MD5");
		String checksum = getFileChecksum(md5Digest, Files.readAllBytes(file.toPath()));

		System.out.println(String.format("[Server] - File received : %s (size=%d), hash=%s", file.getName(),
				file.length(), checksum));

		stream.close();
	}

	private void writeFile(DataOutputStream out, File file) throws IOException {
		byte[] content = Files.readAllBytes(file.toPath());
		out.writeInt(content.length);

		out.writeUTF(file.getName());

		for (int i = 0; i < content.length; i += chunkSize) {
			int size = Math.min(content.length - i, chunkSize);
			byte[] buffer = Arrays.copyOfRange(content, i, Math.min(content.length, i + chunkSize));
			out.write(buffer, 0, size);
			out.flush();
		}
	}

	private static String getFileChecksum(MessageDigest digest, byte[] content) throws IOException {
		digest.update(content);
		byte[] bytes = digest.digest();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}
}
