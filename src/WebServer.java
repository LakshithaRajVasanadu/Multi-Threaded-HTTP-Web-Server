import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

// Class Webserver 
public class WebServer {
	
	private static final String DEFAULT_PORT = "8080";
	private int PORT;
	public String DOCUMENT_ROOT;
	
	public static void main(String[] args) {
		
		// Create new Webserver
		WebServer myWebServer = new WebServer();
		myWebServer.initializeConfig(args);
		myWebServer.start();
	}
	
	public void initializeConfig(String[] args) {
		
		//Initialize default config if any
		HashMap<String, String> configMap = new HashMap<String, String>();
		configMap.put("port", DEFAULT_PORT);
		configMap.put("document_root", null);
		
		//Parse command line parameters
		String key = null;
		String value = null;
		for(int i = 0; i < args.length; i++) {
			//Find key
			if(args[i].startsWith("-")) {
				key = args[i].substring(1);
			}
			//Find value
			if(i+1 < args.length) {
				if(args[i+1].charAt(0) != '-') {
					value = args[i+1];
					configMap.put(key, value);
					i++;
				}
			}
		}
		
		//Validate key value pairs
		try {
			if(configMap.get("document_root") == null) {
				throw(new Exception("Please specify document_root option"));
			} else {
				File documentDirectory = new File(configMap.get("document_root"));
				if( !documentDirectory.exists() || !documentDirectory.isDirectory())
					throw(new Exception("document_root specified is not valid"));
				else
					DOCUMENT_ROOT = configMap.get("document_root");
			}
			if(configMap.get("port") == null) {
				throw(new Exception("Please specify port option"));
			} else {
				int portNumber = Integer.parseInt(configMap.get("port"));
				if(portNumber < 8000 || portNumber > 9999)
					throw(new Exception("port number is not within the range 8000-9999"));
				else
					PORT = portNumber;
			}
			
			//Success
			System.out.println("Document root:" + configMap.get("document_root"));
			System.out.println("Port:" + configMap.get("port") + "\n");
		}
		catch(Exception e) {
			System.out.println(e.getMessage());
			System.exit(-1);
		}
		
	}

	// Start the server and listen for client connections
	public void start(){
		try {
				final ServerSocket serverSocket = new ServerSocket(PORT, 10000);
				
				System.out.println("Web Server started on Port:" + PORT);
				System.out.println("Press Ctrl + C to kill");
				System.out.println("Listening for connections");	
			
				Socket clientSocket;
				while(true) {
					clientSocket = serverSocket.accept();
					
					// Handle request using thread
					new Thread(new RequestHandler(clientSocket, DOCUMENT_ROOT)).start();			
				}
			
			}catch (Exception e) {
				System.out.println("Server could not be started");
			}
	}
}
