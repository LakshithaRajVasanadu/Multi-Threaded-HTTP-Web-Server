import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class HttpResponse {
	private HttpRequest request;
	List<String> responseHeaders = new ArrayList<String>();
	byte[] responseBody = null;
	
	final String document_root;
	
	// HTTP Response Codes
	private static final String SC_OK = "200 OK";
	private static final String SC_BAD_REQUEST = "400 Bad Request";
	private static final String SC_FORBIDDEN = "403 Forbidden";
	private static final String SC_NOT_FOUND = "404 Not Found";

	// HTTP Mime types
    private static final Map<String, String> mimeMap = new HashMap<String, String>() {{
		put("html", "text/html");
		put("css", "text/css");
		put("js", "application/js");
		put("jpg", "image/jpg");
		put("jpeg", "image/jpeg");
		put("png", "image/png");
		put("jif", "image/jif");
		put("txt", "text/plain");
	}};
	
    public HttpResponse(HttpRequest request, String document_root) throws IOException {
    	this.request = request;
    	this.document_root = document_root + "/";
    	generateResponse();
    }
    
    private void generateResponse() throws IOException {
    	if(request.method == null) {
			responseBody = "400 Bad Request".getBytes();
			generateHeaders(SC_BAD_REQUEST, "html", responseBody.length);
			return;
    	}
    	if(!(request.version.equals("HTTP/1.0")) && (!request.version.equals("HTTP/1.1"))) {
			responseBody = "400 Bad Request".getBytes();
			generateHeaders(SC_BAD_REQUEST, "html", responseBody.length);
			return;
    	}
    	
    	if(request.method.equals("GET")) {
    		String file = request.uri.substring(request.uri.indexOf("/")+1);
    		
    		// Append index.html 
    		if(file.equals(""))
    			file = "index.html";	
    		String mime = file.substring(file.indexOf(".")+1);
    		
    		// If path is not in document root
    		Path path = Paths.get(document_root, file);
    		if(!path.startsWith(document_root)) {
    			responseBody = "400 Bad Request".getBytes();
    			generateHeaders(SC_BAD_REQUEST, "html", responseBody.length);
    			return;
    		}
    		
    		// Return if file contains bad string
    		if(file. contains(";") || file.contains("*"))	{
       			responseBody = "400 Bad Request".getBytes();
    			generateHeaders(SC_BAD_REQUEST, "html", responseBody.length);
    			return;
    		}
    		
    		// Check if given file is directory
    		File fileObject = new File(document_root + file);
    		if(fileObject.exists()) {
    			if(fileObject.isDirectory()) {
    				// Append index.html if directory
    				file += "/index.html";
    				fileObject = new File(document_root + file);
    			}
    		}
    			
    		if(fileObject.exists()) {
    			if(fileObject.canRead()) {
    				InputStream is = null;
					try {
						is = new FileInputStream(document_root+file);
					} catch (FileNotFoundException e) {
						responseBody = "404 File Not Found".getBytes();
		    	    	generateHeaders(SC_NOT_FOUND, "html", responseBody.length);
					}
    						
					responseBody = new byte[is.available()];
    				is.read(responseBody);
    		    	generateHeaders(SC_OK, mime, responseBody.length);
    			} 
    			else {
    				responseBody = "403 Forbidden".getBytes();
        	    	generateHeaders(SC_FORBIDDEN, "html", responseBody.length);
    			}
    		}
    		else {
    	    		responseBody = "404 File Not Found".getBytes();
    	    		generateHeaders(SC_NOT_FOUND, "html", responseBody.length);
    		}
    			
    	} else {
			responseBody = "400 Bad Request".getBytes();
			generateHeaders(SC_BAD_REQUEST, "html", responseBody.length);
    	}
    	
    }
    
    // Form headers for response
    private void generateHeaders(String status, String mime, int length) {
    	
    	responseHeaders.add(request.version + " " + status);
    	responseHeaders.add("Content-Type: " + mimeMap.get(mime));
    	responseHeaders.add("Content-Length: " + length); 
    	responseHeaders.add("Date: " + getServerTime());
    	responseHeaders.add("Connection: " + getConnectionType());
    	responseHeaders.add("Server: LakshithaWebServer");
    	
    }
   
    private String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }
    
    
    private String getConnectionType() {
    	boolean keepAlive = request.shouldKeepAlive();
    	if (keepAlive)
            return "keep-alive";
        else 
           return "close";	
    }
    
    // Write to output stream
    public void write(OutputStream os) throws IOException {
		DataOutputStream output = new DataOutputStream(os);
		for (String header : responseHeaders) {
			output.writeBytes(header + "\r\n");
		}
		output.writeBytes("\r\n");
		if (responseBody != null) {
			output.write(responseBody);
		}
		output.writeBytes("\r\n");
	}

}
