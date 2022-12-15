package clavardaj.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
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
				byte[] content;
				System.out.println(len);
				content = readFile(len);
				message = new FileMessage(fileName, content, sender, UserManager.getInstance().getCurrentAgent(),
						LocalDateTime.now());

				// TODO Le mettre dans la frame au clic
				// Stratégie : le stocker dans un dossier temporaire et au moment de
				// l'enregistrement : déplacement et renommage !
				File file = new File(Main.OUTPUT.getAbsolutePath() + File.separator + fileName);
				// TODO Si le nom du fichier existe déjà c'est un problème. Hasher le nom et
				// trouver un moyen pour le retrouver !
				file.createNewFile();

				// On remplit le fichier
				FileOutputStream stream = new FileOutputStream(file);
				stream.write(content);
				stream.close();

			} else {
				String content = in.readUTF();
				message = new TextMessage(content, sender, UserManager.getInstance().getCurrentAgent(),
						LocalDateTime.now());
			}
		} catch (IOException e) {
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
			byte[] buffer = Arrays.copyOfRange(content, i, Math.min(content.length, i + chunkSize));
			out.write(buffer);
			out.flush();
		}
	}

	private byte[] readFile(int len) throws IOException {
		byte[] content = new byte[len];
		byte[] buffer = new byte[chunkSize];

		int index = 0;
		for (int size = len; size > 0; size -= chunkSize) {
			in.read(buffer, 0, Math.min(chunkSize, size));
			System.arraycopy(buffer, 0, content, chunkSize * index, Math.min(chunkSize, size));
			index++;
		}
		return content;
	}

	public void close() throws IOException {
		socket.close();
		in.close();
		out.close();
	}
}
