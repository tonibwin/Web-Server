/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

//isDirectory used for 404
//need to read in a File, therefore use a file class. 
//need to use ANT to build. 
//touch build.xml
//string split command to seperate the second value of "Request line: (GET /test.html HTTP/1.1)"
//convert html to getByte. 

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import javax.imageio.ImageIO;
import java.util.TimeZone;
import java.awt.image.BufferedImage;
public class WebWorker implements Runnable
{

private Socket socket;

/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   String completeTextFile="";
   String completeBinaryNum ="";
   int finalBinary = 0;
   System.err.println("Handling connection...");
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      String grabbedHTMLFile = readHTTPRequest(is);
      BufferedReader readHTMLFile = null;

      String[] p = grabbedHTMLFile.split("\\.");
      if(p[1].equals("html")){
	     try{
		readHTMLFile = new BufferedReader(new FileReader(System.getProperty("user.dir") + grabbedHTMLFile));	

		String line = "";
		while((line = readHTMLFile.readLine()) != null){
		  if (line.equals("<cs371date>")){
		     	Date date = new Date();
			line = date.toString();
		  }
		  if (line.equals("<cs371server>")){
			line = " Destoni's Server";
		  }
		  completeTextFile += line;  	
		}
	
	      }catch(Exception ex) {
		System.out.println("File not found");
		writeContent(os, "404 File Not Found.");
	      }
	      writeHTTPHeader(os,"text/html"); 
 	      writeContent(os , completeTextFile);
      
      }else if(p[1].equals("gif") || p[1].equals("jpg") || p[1].equals("png")){
	      try{
		FileInputStream readFile = new FileInputStream(System.getProperty("user.dir") + grabbedHTMLFile);	
		
		int binaryNum;
		while((binaryNum = readFile.read()) != -1){
			completeBinaryNum += Integer.toString(binaryNum);
			
		}
		finalBinary = Integer.parseInt(completeBinaryNum);
              }catch(Exception ex) {
		System.out.println("File not found\n" + ex);
		writeContent(os, "404 File Not Found.");
	      }
	      writeHTTPHeader(os,"image/" + p[1] ); 
	      writeContentForImage(os , finalBinary);
      	      //writeContent(os, completeBinaryNum);
	}
           
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
**/
private String readHTTPRequest(InputStream is) throws Exception
{
   String line;
   String completeTextFile = "";
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   String Path = "";
      try {
        Path = r.readLine();
        String[] p = Path.split(" ");
        return p[1];
      } catch (Exception e) {  
	 System.err.println("Request error: "+e);
      }
   return "";
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
{
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   os.write("HTTP/1.1 200 OK\n".getBytes());
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   os.write("Server: Destoni's\n".getBytes());
   //os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   //os.write("Content-Length: 438\n".getBytes()); 
   os.write("Connection: close\n".getBytes());
   os.write("Content-Type: ".getBytes());
   os.write(contentType.getBytes());
   os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
   return;
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os, String writeHTML) throws Exception
{
   os.write(writeHTML.getBytes());
}

private void writeContentForImage(OutputStream os, int imageFile) throws Exception
{
	os.write(imageFile);
}

} // end class
