// Grupo 16 LEI FCUL REDES19/20
import java.io.*;
import java.net.*;

class server{
	public static void main (String argv[]) throws Exception
	{
		//creates welcome socket (the door to knock) -- only used to accept connections
		ServerSocket welcomeSocket = new ServerSocket(6789);

		while(true){
			//creates connection socket -- one per TCP connection
			Socket connectionSocket = welcomeSocket.accept();

			//creates input and output streams (to receive from/send to client socket)
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			//Read Request from client and display it
			String line;
			do {
				line = inFromClient.readLine();
				System.out.println(line);
			} while (!line.equals(""));
			
			//closing
			connectionSocket.close();
			inFromClient.close();
			outToClient.close();
		}
	}	
}