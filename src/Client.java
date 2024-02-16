import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

// Application client
public class Client {
	private static Socket socket;

	static Scanner sc = new Scanner(System.in);

	static String inputUsername() {
		System.out.println("\nEnter a username");
		String username = sc.nextLine();
		return username;
	}

	static String inputPassword() {
		System.out.println("Enter a password");
		String password = sc.nextLine();
		return password;
	}

	static int inputListeningPort() {
		System.out.println("Enter a listening port between 5000 and 5050: ");
		String listeningPort = sc.nextLine();
		return Integer.parseInt(listeningPort);
	}

	static boolean isListeningPortValid(int listeningPort) {
		if (listeningPort < 5000 || listeningPort > 5050) {
			System.out.println("The listening port is out of line");
			return false;
		}
		return true;
	}

	static String inputServerAddress() {
		System.out.println("Enter an IP address: ");
		String IPAddress = sc.nextLine();
		return IPAddress;
	}

	static boolean isServerAddressValid(String IPAddress) {
		String[] splitedIPAddress = IPAddress.split("\\.");

		if (splitedIPAddress.length != 4) {
			System.out.println("The IP address is not valid");
			return false;
		}

		for (String split : splitedIPAddress) {
			if (Integer.parseInt(split) > 255 || Integer.parseInt(split) < 0) {
				System.out.println("The IP address is not valid");
				return false;
			}
		}

		return true;
	}

	public static void main(String[] args) throws Exception {

		// Adresse et port du serveur
		String serverAddress = inputServerAddress();
		while (!isServerAddressValid(serverAddress)) {
			serverAddress = inputServerAddress();
		}

		int port = inputListeningPort();
		while (!isListeningPortValid(port)) {
			port = inputListeningPort();
		}

		// Connexion + validation du client

		// Création d'une nouvelle connexion aves le serveur
		socket = new Socket(serverAddress, port);

		System.out.format("Server launched sur [%s:%d]", serverAddress, port);

		String username = inputUsername();
		String password = inputPassword();

		// Céatien d'un canal entrant pour recevoir les messages envoyés, par le serveur
		DataInputStream in = new DataInputStream(socket.getInputStream());
		DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // création de canal d’envoi
		out.writeUTF(username);
		out.writeUTF(password);

		// Attente de la réception d'un message envoyé par le, server sur le canal
		String helloMessageFromServer = in.readUTF();
		System.out.println(helloMessageFromServer);
		String lastMessages = in.readUTF();
		System.out.println(lastMessages);

		// Thread to send a message
		new Thread(() -> {
			try {
				while (true) {
					String message = sc.nextLine();
					while (message.length() > 200) {
						System.out.println("The message can't contain more than 200 characters");
						message = sc.nextLine();
					}
					out.writeUTF(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}).start();

		new Thread(() -> {
			try {
				while (true) {
					String message = in.readUTF();
					System.out.println(message);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
}