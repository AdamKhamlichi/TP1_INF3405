import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Serveur {
	static Scanner sc = new Scanner(System.in);
	private static Socket socket;
	private static ServerSocket listener;
	private static Map<String, String> clientsBook;
	private static ConcurrentHashMap<Socket, DataOutputStream> clients;
	private static final String LOG_FILE_PATH = "data/log.json";

	private static void saveClient(String username, String password) {
		clientsBook.put(username, password);
		saveLogToJson(username,password);
		System.out.println("Client " + username + " was saved");
	}

	// Save log to JSON file
	public static void saveLogToJson(String clientName, String clientData) {
        String beginning = "{\"clients\": [";
        String ending = "\n]}";
        StringBuilder clientsContent = new StringBuilder();

        try {
            String existingContent = new String(Files.readAllBytes(Paths.get(LOG_FILE_PATH)));
            // Check if file already contains the beginning, to not duplicate it
            if (existingContent.contains(beginning)) {
                // Remove ending to append new client data
                existingContent = existingContent.substring(0, existingContent.length() - ending.length());
                clientsContent.append(existingContent);
            } else {
                clientsContent.append(beginning);
            }
            // Append new client data
            if (!clientsContent.toString().endsWith("[")) {
                // Add a comma if it's not the first entry
                clientsContent.append(",");
            }
            clientsContent.append(String.format("\n        {\"%s\": \"%s\"}", clientName, clientData));

        } catch (IOException e) {
            // If reading fails, start with the beginning
            clientsContent.append(beginning);
            clientsContent.append(String.format("\n        {\"%s\": \"%s\"}", clientName, clientData));
        }

        // Close the JSON structure
        clientsContent.append(ending);

        // Write the updated content back to the file
        try (FileWriter fileWriter = new FileWriter(LOG_FILE_PATH, false)) { // Overwrite the file
            fileWriter.write(clientsContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}


	// Check if the client username and password are valid
	public static boolean isClientValid(String username, String password) {
		if (!clientsBook.containsKey(username)) {
			saveClient(username, password);
		}
		return clientsBook.get(username).equals(password);
	}

	// Main method
	public static void main(String[] args) throws Exception {
		// Initialize variables
		int clientNumber = 0;
		clientsBook = new HashMap<>();
		clients = new ConcurrentHashMap<>();
		listener = new ServerSocket();

		// Get server address and port
		String serverAddress = inputIPAddress();
		int serverPort = inputServerPort();

		// Bind server socket to address and port
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		listener.bind(new InetSocketAddress(serverIP, serverPort));
		System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);

		try {
			while (true) {
				socket = listener.accept();
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());

				String username = in.readUTF();
				String password = in.readUTF();

				if (isClientValid(username, password)) {
					new ClientHandler(socket, clientNumber++, clients, username).start();
				} else {
					out.writeUTF("The password is not valid");
				}
			}
		} finally {
			listener.close();
		}
	}

	// Method to input server IP address
	static String inputIPAddress() {
		System.out.println("Enter an IP address: ");
		String IPAddress = sc.nextLine();
		return IPAddress;
	}

	// Method to input server port
	static int inputServerPort() {
		System.out.println("Enter a listening port between 5000 and 5050: ");
		int port = sc.nextInt();
		while (port < 5000 || port > 5050) {
			System.out.println("Invalid port number. Enter a port between 5000 and 5050: ");
			port = sc.nextInt();
		}
		return port;
	}
}