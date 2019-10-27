import java.io.*;
import java.net.*;

class client{
	public static void main (String argv[]) throws Exception
	{
		String sentence;
		String modifiedSentence;

		//Creating client socket
		Socket clientSocket = new Socket("localhost",6789);

		//Creating streams for keyboard input, to output characters to the server, and to receive from the socket
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		//Read sentence from keyboard, send to server socket
		// sentence = inFromUser.readLine();
		// outToServer.writeBytes(sentence + '\n');
		outToServer.writeBytes("GET /help HTTP/1.0\r\n");
		outToServer.writeBytes("Host: localhost:6789\r\n");
		outToServer.writeBytes("\r\n");
		
		//Receive replies from server and print them
		for (int i = 0; i < 10; i++) {
			modifiedSentence = inFromServer.readLine();
			System.out.println("FROM SERVER: " + modifiedSentence);
		}

		//closing
		inFromUser.close();
		inFromServer.close();
		outToServer.close();
		clientSocket.close();
	}
}