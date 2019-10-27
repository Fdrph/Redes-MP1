import java.io.*;
import java.net.*;

class mtServer implements Runnable {


	Socket currentConnection;		// Socket of the connection to the client (per thread)
	BufferedReader inFromClient;	// Stream that receives from client       (per thread)
	DataOutputStream outToClient;	// Stream that sends messages to client   (per thread)
	
	public mtServer(Socket s) {
		currentConnection = s;
	}
	public static void main (String argv[]) throws Exception
	{
		//creates the welcome socket (the door to knock on)
		ServerSocket welcomeSocket = new ServerSocket(6789);

		// Upon a Ctrl+C Interrupt we want to catch it and close the opened socket gracefully
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					System.out.print("\nShutting down ...\n");
					welcomeSocket.close();
				} catch (IOException e) { e.printStackTrace(); }
			}
		});
	
		// Keep waiting for connections and accepting them.
		// For each accepted connection create a new thread to handle the request.
		while (true) {
			try {
				Socket currentConnection = welcomeSocket.accept();
				Thread thread = new Thread(new mtServer(currentConnection));
				thread.start();
			} catch (Exception e) { e.printStackTrace(); }
		}
	}

	@Override
    public void run() {
		try {
			//creates input and output streams (to receive from/send to client socket)
			inFromClient = new BufferedReader(new InputStreamReader(currentConnection.getInputStream()));
			outToClient = new DataOutputStream(currentConnection.getOutputStream());
			processRequest();
		} catch (SocketTimeoutException e) {
			System.out.println("CLIENT TIMED OUT!");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
	
	/** 
	/* Reads an incoming request and parses it
	/* @return an integer which is the Status-Code in the RFC2616 6.1.1
	*/
	private int processRequest() throws Exception {

		// Read the request
		System.out.println("FROM CLIENT:"+currentConnection.getRemoteSocketAddress().toString());
		String line;
		do {
			line = inFromClient.readLine();
			System.out.println(line);
		} while (!line.equals(""));

		//closing
		closeConnection();
		return 200;
	}

	private void closeConnection() {
		try{
			inFromClient.close();
			outToClient.close();
			currentConnection.close();
		} catch(IOException e) {e.printStackTrace();}
	}
}