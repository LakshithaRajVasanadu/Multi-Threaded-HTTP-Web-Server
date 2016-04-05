import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class RequestHandler implements Runnable {
	Socket socket = null;
	final String document_root;
	
	// Maximum number of connections that server can maintain
	static final int MAX_SERVER_LOAD = 50;
	
	// Keep track of number of keep alive connections for HTTP 1.1
	static int numberOfKeepAliveConnections = 0;
	
	// Synchronize all methods for shared counter
	public static synchronized void incrementNumberOfKeepAliveConnections() {
		numberOfKeepAliveConnections ++;
	}
	
	public static synchronized void decrementNumberOfKeepAliveConnections() {
		numberOfKeepAliveConnections--;
	}
	
	public static synchronized int getNumberOfKeepAliveConnections() {
		return numberOfKeepAliveConnections;
	}
	
	// Dynamic timeout based on server load
	public static int getTimeoutDuration() {
		if(numberOfKeepAliveConnections <= MAX_SERVER_LOAD)
			return 50000;
		else 
			return 30000;
	}
	
	public RequestHandler(Socket clientSocket, String DOCUMENT_ROOT) {
		this.socket = clientSocket;
		this.document_root = DOCUMENT_ROOT;
	}

	@Override
	public void run() {
		InputStream	is = null;
		OutputStream os;  
		HttpRequest request;
		HttpResponse response;
		
		try {
			RequestHandler.incrementNumberOfKeepAliveConnections();
			
			// Set timeout on connection socket
			socket.setSoTimeout(getTimeoutDuration());
			
			while(true) {
				is = socket.getInputStream();
				os = socket.getOutputStream();
				
				// Parse Request
				request  = new HttpRequest(is, MAX_SERVER_LOAD);	      
				System.out.println("Got Request:");
				request.print();
	        
				// Get Response
				response = new HttpResponse(request, document_root);
				response.write(os);
			
				// Check if connection has to be persistent or not
				if(!request.shouldKeepAlive())
					break;
			}
		
			// Close connection
			is.close();
			os.close();
			
			// Decrement number of alive connections
			RequestHandler.decrementNumberOfKeepAliveConnections();
			
		} catch(SocketTimeoutException e) {
			try {
				// Close socket if timed out
				socket.close();
				RequestHandler.decrementNumberOfKeepAliveConnections();
			}catch (Exception e1) { }
		}catch (IOException e) { }
	}
}
