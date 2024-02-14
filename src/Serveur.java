import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Serveur {
	
	static Scanner sc = new Scanner(System.in);
	private static Socket socket;
	
	private static ServerSocket Listener;
	private static Map<String, String> clientsBook;
	private static ConcurrentHashMap<Socket, DataOutputStream> clients;
	private static MessageLogger usernameLog = new MessageLogger("username.txt");
	private static MessageLogger passwordLog = new MessageLogger("password.txt");


	private static void saveClient(String username, String password) {
		//clientsBook.put(username, password);
//		for(String client: clientsBook.keySet()) {
//			System.out.println(client + clientsBook.get(client));
//		}
		usernameLog.write(username);
		passwordLog.write(password);
		System.out.println("Client " + username + " was saved");
	}
	// Application Serveur
	
	static int getServerListeningPort(DataInputStream in) throws IOException {
		return in.readInt();
	}
	
	static int inputServerPort() {
		System.out.println("Enter un port d'écoute 5000 and 5050: ");
		String listeningPort = sc.nextLine();
		return Integer.parseInt(listeningPort);
	}
	
	static boolean isListeningPortValid(int listeningPort) {
		if(listeningPort<5000 || listeningPort>5050) {
			System.out.println("The listening port is out of line");
			return false;
		}
		if(!sc.hasNextInt()) {
			System.out.println("The listening port cannot be a string");
			return false;
		}
		return true;
	}
	
	static String inputIPAddress() {
		System.out.println("Enter an IP address: ");
		String IPAddress = sc.nextLine();
		return IPAddress;
	}
	
	static boolean isIPAddressValid(String IPAddress) {
		String[] splitedIPAddress = IPAddress.split("\\.");
		
		if(splitedIPAddress.length !=4) {
			System.out.println("The IP address is not valid");
			return false;
		}
		
		for(String split: splitedIPAddress) {
			if(Integer.parseInt(split)>255 || Integer.parseInt(split)<0) {
				System.out.println("The IP address is not valid");
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isClientValid(String username, String password) {
//		if(!clientsBook.containsKey(username)) {
//			saveClient(username, password);
//			return true;
//		}
		if(usernameLog.contains(username) == -1) {
			saveClient(username, password);
			return true;
		}
		if(usernameLog.contains(username) == passwordLog.contains(password)){
			return true;
		} 
//		if(clientsBook.get(username).equals(password)){
//			return true;
//		}
		else {
			System.out.println("The password is not valid");
			return false;
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		// Compteur incrémenté à chaque connexion d'un client au serveur
		int clientNumber = 0;
		clientsBook = new HashMap<String, String>();
		clients = new ConcurrentHashMap<Socket, DataOutputStream>();

		// Adresse et port du serveur
		String serverAddress = inputIPAddress();
		int serverPort = inputServerPort();
		 
		// Création de la connexien pour communiquer avec les, clients
		Listener = new ServerSocket();
		Listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		
		// Association de l'adresse et du port à la connexien
		Listener.bind(new InetSocketAddress(serverIP, serverPort));
		System.out.format("The server is running on %s:%d%n", serverAddress, serverPort);
		try {
			
			// À chaque fois qu'un nouveau client se, connecte, on exécute la fonstion
			// run() de l'objet ClientHandler
			while (true) {
				
				// Important : la fonction accept() est bloquante: attend qu'un prochain client se connecte
				// Une nouvetle connection : on incrémente le compteur clientNumber
				socket = Listener.accept();
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				
				// Reception du nom utilisateur + mot de passe
				String username = in.readUTF();
				String password = in.readUTF();
//				System.out.println(username);
//				System.out.println(password);
				// Vérification de la validité du client
				if(isClientValid(username, password)) {
					new ClientHandler(socket, clientNumber++, clients, username).start();
				}
				else {
					out.writeUTF("The password is not valid");
				}
				
			}
		} finally {
			
			// Fermeture de la connexion
			Listener.close();
		}
	} 
}