import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
	HashMap<String, String> headers = new HashMap<String, String>();
	String method;
	String uri;
	String version;
	final int MAX_SERVER_LOAD;
	
	public HttpRequest(InputStream is, int MAX_SERVER_LOAD) throws IOException {
		this.MAX_SERVER_LOAD = MAX_SERVER_LOAD;
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
		// Waits and Reads Input, Parse request line and headers
		// Handles only GET request format
		String line1 = reader.readLine();
		while(line1 == null || line1.equals("\n") || line1.equals("\r") || (line1.length() == 0) ) {
			line1 = reader.readLine();
		}
		
		// Parse First line of request
		String line = line1;
		if(line != null && line.length() > 0)
			parseRequestFirstLine(line);

		// Parse headers if any
		while((line = reader.readLine()) != null && line.length() > 0) {
			parseRequestHeader(line);
		} 
	}
	
	// Print HTTP request
	public void print() {
		System.out.println("Method:" + method);
		System.out.println("URI:" + uri);
		System.out.println("Version:" + version);
		
		for (Map.Entry<String, String> entry : headers.entrySet()) {
		    System.out.println(entry.getKey() + ":" + entry.getValue());
		}
		
		System.out.println("\n");
	}

	// Parse first line of request
	private void parseRequestFirstLine(String line) {
		String[] tokens = line.split("\\s+");
		if(tokens.length == 3) {
			method = tokens[0];
			uri = tokens[1];
			version = tokens[2];
		}
	}

	// Parse headers
	private void parseRequestHeader(String line) {
		String[] tokens = line.split(": ");
		if(tokens.length == 2) {
			headers.put(tokens[0], tokens[1]);
		}
	}
	
	// Check if connection has to be alive or not
	public boolean shouldKeepAlive() {
		boolean keepAlive = false;
		
		//If HTTP 1.0 - default is close connection
		//If HTTP 1.0 - connection keep alive header, keep alive connection
		//If HTTP 1.1 - default keep alive connection
		//If HTTP 1.1 - connection close header, close connection
		
		
		boolean hasConnectionKeepAlive = headers.containsKey("Connection") &&
		            						headers.get("Connection").contains("keep-alive");
		boolean hasConnectionClose = headers.containsKey("Connection") &&
		            					headers.get("Connection").contains("close");
		
		if("HTTP/1.0".equals(version)) {
			if(hasConnectionKeepAlive) 
				keepAlive = true;
		}
		else if("HTTP/1.1". equals(version)) {
			if(!hasConnectionClose)
				keepAlive = true;
		}
		 
		return keepAlive;
	 }

}
