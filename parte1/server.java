import java.io.*;
import java.net.*;

class server{
	public static void main (String argv[]) throws Exception
	{
		String clientSentence;
		String capitalizedSentence;

		//creates welcome socket (the door to knock) -- only used to accept connections
		ServerSocket welcomeSocket = new ServerSocket(6789);

		while(true){
			//creates connection socket -- one per TCP connection
			Socket connectionSocket = welcomeSocket.accept();

			//creates input and output streams (to receive from/send to client socket)
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

			//the server task -- capitalizing sentence and sending it several times to the client
			clientSentence = inFromClient.readLine();
			capitalizedSentence = clientSentence.toUpperCase() + '\n';
		
			for (int i = 0; i < 10; i++) {
				outToClient.writeBytes(capitalizedSentence);
				//Sleep for 1 second to emulate some complicated processing in the server 
				Thread.sleep(1000);
			}
			
			//closing
			connectionSocket.close();
			inFromClient.close();
			outToClient.close();
		}
	}	
}