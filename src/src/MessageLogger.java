package src;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedWriter;


public class MessageLogger {

	private String path;
	
	public MessageLogger(String path) {
		this.path = path;
	}
	
	public void logMessage(String message) {
		try {
			FileWriter fw = new FileWriter(path, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			out.println(message);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not log" + e.getMessage());
		}
	}
}
