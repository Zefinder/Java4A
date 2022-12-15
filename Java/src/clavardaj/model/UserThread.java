package clavardaj.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;

import clavardaj.Main;
import clavardaj.controller.UserManager;

/**
 * 
 * @author nicolas
 *
 */
public abstract class UserThread {

	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;

	// En réalité le buffer de la socket fait 64ko mais on réduit de moitié pour ne
	// pas surcharger la socket !
	private final int chunkSize = 0x8000;

	public UserThread(Socket socket) {
		this.socket = socket;
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Message read(Agent sender, boolean isFile) {
		Message message = null;
		String fileName = "";
		try {
			if (isFile) {
				fileName = in.readUTF();
				int len = in.readInt();
				String newName;
				System.out.println(len);
				newName = readFile(len, fileName);
				message = new FileMessage(fileName, newName.getBytes(), sender,
						UserManager.getInstance().getCurrentAgent(), LocalDateTime.now());

			} else {
				String content = in.readUTF();
				message = new TextMessage(content, sender, UserManager.getInstance().getCurrentAgent(),
						LocalDateTime.now());
			}
		} catch (IOException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return message;
	}

	public void write(Message message) {
		try {
			if (message instanceof FileMessage) {
				writeFile((FileMessage) message);
			} else
				out.writeUTF(((TextMessage) message).getStringContent());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeFile(FileMessage message) throws IOException {
		out.writeUTF(((FileMessage) message).getFileName());
		byte[] content = message.getContent();
		out.writeInt(content.length);

		for (int i = 0; i < content.length; i += chunkSize) {
			int size = Math.min(content.length, i + chunkSize);
			byte[] buffer = Arrays.copyOfRange(content, i, Math.min(content.length, i + chunkSize));
			out.write(buffer, 0, size);
			out.flush();
		}
	}

	// Retourne le nom temporaire du fichier
	private String readFile(int len, String fileName) throws IOException, NoSuchAlgorithmException {
		byte[] buffer = new byte[chunkSize];

		File file = new File(Main.OUTPUT.getAbsolutePath() + File.separator + fileName);
		// TODO Si le nom du fichier existe déjà c'est un problème. Hasher le nom et
		// trouver un moyen pour le retrouver !
		file.createNewFile();

		// On remplit le fichier
		FileOutputStream stream = new FileOutputStream(file);

		for (int size = len; size > 0; size -= chunkSize) {
			int byteRead = in.read(buffer, 0, Math.min(chunkSize, size));
			stream.write(buffer, 0, byteRead);
		}

		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		byte[] encodedhash = digest.digest(fileName.getBytes(StandardCharsets.UTF_8));

		stream.close();
		return "";
	}

	public void close() throws IOException {
		socket.close();
		in.close();
		out.close();
	}
}
