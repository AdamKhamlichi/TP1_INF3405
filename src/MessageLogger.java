
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.stream.Stream;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;


public class MessageLogger {

	private Path path;
	private String stringPath;
	
	public MessageLogger(String path) {
		this.path = Paths.get(path);
		this.stringPath = path;
	}
	
	public void logMessage(String message) {
		try {
			String filename= stringPath;
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    fw.write(message + '\n');//appends the string to the file
		    fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}
	
	public int getNbLines() {
		long lineCount = 0;
		try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
		  lineCount = stream.count();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (int)lineCount;
		
	}
	
	public String loadMessage(int nLine) {
		try {
			return Files.readAllLines(Paths.get(this.stringPath)).get(nLine);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error loading message";
		}
	}
	public void write(String message) {
		try {
			String filename= stringPath;
		    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
		    fw.write(message + '\n');//appends the string to the file
		    fw.close();
		} catch (IOException ioe) {
			System.err.println("IOException: " + ioe.getMessage());
		}
	}
	public int contains(String stringToFind) {
		try {
		    Scanner scanner = new Scanner(path);

		    //now read the file line by line...
		    int lineNum = 0;
		    while (scanner.hasNextLine()) {
		        String line = scanner.nextLine();
		        lineNum++;
		        if(line.contains("\\b"+stringToFind+"\\b")) { 
		            System.out.println("on line " +lineNum);
		            return lineNum;
		        }
		    }
		    return -1;
		    
		} catch(IOException ioe) { 
			System.err.println("IOException: " + ioe.getMessage());
			return -2;
		}
	}
	
}
