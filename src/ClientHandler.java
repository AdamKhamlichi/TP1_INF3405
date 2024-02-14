import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler extends Thread { // pour traiter la demande de chaque client sur un socket particulier
	private Socket socket;
	private int clientNumber;
	private final ConcurrentHashMap<Socket, DataOutputStream> clients;
	private String username;
	private List<String> messages = new ArrayList<>();
	private static MessageLogger messageLog = new MessageLogger("messages.txt");
	
	public ClientHandler(Socket socket, int clientNumber, ConcurrentHashMap<Socket, DataOutputStream> clients, String username) {
		this.socket = socket;
		this.clientNumber = clientNumber; 
		this.clients = clients;
		this.username = username;
		System.out.println("New connection with client#" + clientNumber + " at" + socket);}

	public void run() { // Création de thread qui envoi un message à un client
		try {
			DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // création de canal d’envoi
			DataInputStream in = new DataInputStream(socket.getInputStream());
			clients.put(socket, new DataOutputStream(socket.getOutputStream())); //création d'un canal d'envoi par client
			
			out.writeUTF("Hello from server - " + this.username + " you are client #" + clientNumber);
			
			// envoie au client qui vient de se connecter les 15 derniers messages
			loadMessages(out);

			String message;
			while(true) {
				message = in.readUTF();
				messages.add(message);
				messageLog.logMessage(this.formatMessage(message));
				System.out.println("Received: "+ message);
				broadcastMessage(message);
			}
		}// envoi de message}
			catch (IOException e) {
			System.out.println("Error handling client# " + clientNumber + ": " + e);
		} finally {
			clients.remove(socket);
			try {
				socket.close();
			} catch (IOException e) {
				System.out.println("Couldn't close a socket, what's going on?");}
			System.out.println("Connection with client# " + clientNumber+ " closed");
			}
		}
	private String formatMessage(String message) {
		Date currentDate = new Date();
		return "[" + username + " - "+ socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + currentDate + "]: " + message;
	}
	
	private void loadMessages(DataOutputStream out) {
		int nbMessages = messageLog.getNbLines();
		
		int startingMessage = nbMessages - 15;
		
		if(nbMessages<15) {
			for(int i=0; i<nbMessages; i++) {
				try {
					out.writeUTF(messageLog.loadMessage(i));
				} catch (IOException e) {
					System.err.println("Error loading message: "+ e.getMessage());
					e.printStackTrace();
				}
			}
		}
		else {
			for(int i=startingMessage; i<startingMessage+15; i++) {
				try {
					System.out.println("Here are the 15 last messages");
					out.writeUTF(messageLog.loadMessage(i));
				} catch (IOException e) {
					System.err.println("Error loading message: "+ e.getMessage());
					e.printStackTrace();
				}
			}
		}
		
		
	}

	private void broadcastMessage(String message) {
		for(DataOutputStream out: clients.values()) {
			try {
				//Date currentDate = new Date();
				//out.writeUTF("[" + username + " - "+ socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + currentDate + "]: " + message);
				out.writeUTF(this.formatMessage(message));
				System.out.println(this.formatMessage(message));
			}
			catch(IOException e) {
				System.err.println("Error occured broadcasting message to client: " + e.getMessage());
			}
		}
		
	}
	
}