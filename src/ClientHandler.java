
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler extends Thread {
	private Socket socket;
	private int clientNumber;
	private final ConcurrentHashMap<Socket, DataOutputStream> clients;
	private String username;
	private static final String MESSAGE_FILE_PATH = "data/messages.txt";

	public ClientHandler(Socket socket, int clientNumber, ConcurrentHashMap<Socket, DataOutputStream> clients,
			String username) {
		this.socket = socket;
		this.clientNumber = clientNumber;
		this.clients = clients;
		this.username = username;
		System.out.println("New connection with client #" + clientNumber + " at " + socket);
	}

	public void run() {
	    try {
	        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
	        DataInputStream in = new DataInputStream(socket.getInputStream());
	        clients.put(socket, new DataOutputStream(socket.getOutputStream()));

	        out.writeUTF("Hello from server - " + this.username + ", you are client #" + clientNumber);
	        loadLastMessages(out);

	        String message;
	        while (true) {
	            message = in.readUTF();
	            String formattedMessage = String.format("[%s - %s:%d - %s]: %s%n", username,
						socket.getInetAddress().getHostAddress(), socket.getPort(), getCurrentDateTime(), message);
	            if ("exit".equalsIgnoreCase(message)) {
			        out.writeUTF("Disconnected");
	                break; // Exit the loop and close the connection
	            }
	            broadcastMessage(formattedMessage);
	            appendToLogFile(formattedMessage);
	        }
	    } catch (IOException e) {
	        System.out.println("Error handling client #" + clientNumber + ": " + e);
	    } finally {
	        try {
	            socket.close();	        
	            clients.remove(socket);

	        } catch (IOException e) {
	            System.out.println("Couldn't close a socket, what's going on?");
	        }
	        System.out.println("Connection with client #" + clientNumber + " closed");
	    }
	}

	private void loadLastMessages(DataOutputStream out) {
		try (RandomAccessFile file = new RandomAccessFile(MESSAGE_FILE_PATH, "r")) {
			long fileLength = file.length();
			long position = fileLength - 1;
			int count = 0;
			StringBuilder messages = new StringBuilder();

			while (position >= 0 && count < 15) {
				file.seek(position);
				char c = (char) file.read();
				if (c == '\n') {
					messages.insert(0, file.readLine() + "\n");
					count++;
				}
				position--;
			}

			out.writeUTF("Here are the last 15 messages:\n" + messages.toString());
		} catch (IOException e) {
			System.err.println("Error loading last messages: " + e.getMessage());
		}
	}

	private void broadcastMessage(String message) {
		for (DataOutputStream out : clients.values()) {
			try {
				out.writeUTF(message);
			} catch (IOException e) {
				System.err.println("Error broadcasting message to client: " + e.getMessage());
			}
		}
	}

	private void appendToLogFile(String message) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(MESSAGE_FILE_PATH, true))) {
			writer.write(message);
		} catch (IOException e) {
			System.err.println("Error appending message to log file: " + e.getMessage());
		}
	}

	private String getCurrentDateTime() {
		LocalDateTime now = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd@HH:mm:ss");
		return now.format(formatter);
	}
}
