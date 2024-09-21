import java.io.*;
import java.net.*;
import java.util.*;
public class DNSClient{
    public static void main(String[]args)
            throws UnknownHostException, IOException{
	String host = "localhost";
	int port = 5001;
	//Create new client socket and connect to the server.
	Socket cSock = new Socket(host, port);
	//Output Stream: gửi dữ liệu đến Server
	PrintWriter sendOut = new PrintWriter(cSock.getOutputStream(), true);
	//Input Stream: nhận dl từ Server
	BufferedReader readIn = new BufferedReader(
	    new InputStreamReader(cSock.getInputStream()));
	Scanner inLine = new Scanner(System.in);
	String query = "";        
	while(true){
            System.out.println("Type in a domain name to query, or 'q' to quit:");
            query = inLine.nextLine(); //Lưu trữ DNS mà người dùng nhập
            if (query.equalsIgnoreCase("q") || query.equalsIgnoreCase("quit")) {
                sendOut.println("hangup");//tell the server to disconnect
                sendOut.close();
                readIn.close();
                cSock.close();
                System.exit(0);
            }
            else{
                sendOut.println(query);
                String data = readIn.readLine();
                System.out.println("Received: '" + data + "'\n");
            }
        }
    }
}
