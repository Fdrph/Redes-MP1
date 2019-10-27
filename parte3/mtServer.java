import java.io.*;
import java.net.*;
import java.nio.file.Files;

class mtServer implements Runnable {

	String[][] statusCodes = {	{"200","OK"},
								{"400","Bad Request"},
								{"404","Not Found"},
								{"408","Request Timeout"},
								{"501","Method Unimplemented"},
								{"505","HTTP Version Not Supported"}
							 };

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
				// currentConnection.setSoTimeout(10*1000); // set time out to 10 seconds
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
			int code = processRequest();
			if(code != 200) {sendErrorResponse(code);}
		} catch (SocketTimeoutException e) {
			System.out.println("CLIENT TIMED OUT!");
			sendErrorResponse(408);
		} catch (Exception e) {
			System.out.println("HERE3~!");
			e.printStackTrace();
		} finally {
			closeConnection();
		}
	}
	
	/** Request-Line   = Method SP Request-URI SP HTTP-Version CRLF
	/* Reads an incoming request and parses it, checking for correctness.
	/* @return an integer which is the Status-Code in the RFC2616 6.1.1
	*/
	private int processRequest() throws Exception {

		// Read the Request-Line
		String reqLine = inFromClient.readLine();
		if (reqLine == null) {return 200;}
		
		// Checking for request correctness
		String fields[] = reqLine.split("\\s");
		if (reqLine.length() == 0 || Character.isWhitespace(reqLine.charAt(0)) || fields.length != 3) {return 400;}
		if( fields[2].indexOf("HTTP/") != 0 || fields[2].indexOf(".") != 6) {return 400;}
		String[] temp = fields[2].substring(5).split("\\.");
		int i,j;
		try { 
			i = Integer.parseInt(temp[0]);
			j = Integer.parseInt(temp[1]);
		} catch (NumberFormatException e) {return 400;}	// Version numbers are integers
		if (i < 0 || j < 0) {return 400;}
		if (i != 1 || (j != 0 && j != 1) ) {return 505;} // We only support HTTP 1.0 and 1.1
		String method = fields[0];
		if 	(	method.equals("OPTIONS")	|| method.equals("HEAD") 	|| 
				method.equals("POST") 		|| method.equals("PUT")		|| 
				method.equals("DELETE")		|| method.equals("TRACE")	|| 
				method.equals("CONNECT")) {return 501;}
		if (!method.equals("GET")) {return 400;}
		if (fields[1].indexOf("/") != 0) {return 400;}

		// Read the Headers
		String header;
		do {
			header = inFromClient.readLine();
			// String h[] = header.split("\\:");
		} while (!header.equals(""));

		// Read the URI
		String uri = fields[1];
		String path = System.getProperty("user.dir");
		File requestedResource = new File(path + uri);
		if(!requestedResource.exists() || requestedResource.isDirectory()) {return 404;} // Check if requested resource exists on disk
		byte[] fileContent = Files.readAllBytes(requestedResource.toPath());
		
		sendResponse(fileContent);

		//closing
		closeConnection();
		return 200;
	}

	// Send Body of a 200 OK HTTP-response
	private void sendResponse(byte[] body) {
		try{
			outToClient.writeBytes("HTTP/1.1 200 OK\r\n");
			outToClient.writeBytes("Content-Length: "+ body.length +"\r\n");
			outToClient.writeBytes("\r\n");
			outToClient.write(body);
		} catch (IOException e) {e.printStackTrace();}
	}

	// Send HTTP-responses that contain errors
	private void sendErrorResponse(int statusCode) {
		String msg = "";
		int i;
		for (i=0; i<statusCodes.length; i++) {
			if (statusCodes[i][0].equals(""+statusCode)) {
			  msg = statusCode + " " + statusCodes[i][1];
			  break;
			}
		  }
		try {
			// Status-Line
			int size = msg.length()+1;
			outToClient.writeBytes("HTTP/1.1 "+msg+"\r\n");
			outToClient.writeBytes("Content-Length: "+ size +"\r\n");
			outToClient.writeBytes("\r\n");
			outToClient.writeBytes(msg+"\n");
		} catch (IOException e) {e.printStackTrace();}
	}

	private void closeConnection() {
		try{
			inFromClient.close();
			outToClient.close();
			currentConnection.close();
		} catch(IOException e) {e.printStackTrace();}
	}
}