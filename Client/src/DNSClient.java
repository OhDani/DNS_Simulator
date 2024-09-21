/**
 * DNS Client
 * CSci 4211 - fall 2016
 */
import java.io.*;
import java.net.*;
import java.util.*;

/*
 * Class DNSClient is one of three distributed programs. The client sends DNS
 * inquiries to the server. The server does a lookup of the information and
 * returns it.
 */

public class DNSClient{
    public static void main(String[]args)
            throws UnknownHostException, IOException{
	//Change the host String to recognize the address where the server
	String host = "localhost";
	//Change the port number to match the port number opened on the server.
	int port = 5001;
	//Create new client socket and connect to the server.
	Socket cSock = new Socket(host, port);
	//Output Stream: gửi dữ liệu đêến Server
	PrintWriter sendOut = new PrintWriter(cSock.getOutputStream(), true);
	//Input Stream: nhận dl từ Server
	BufferedReader readIn = new BufferedReader(
	    new InputStreamReader(cSock.getInputStream()));
	Scanner inLine = new Scanner(System.in);
	String query = "";        
	while(true){
            //Request DNS query from user
            System.out.println("Type in a domain name to query, or 'q' to quit:");
            //send DNS query to the server. Change the URL to whatever you want to
            //query (ex. google.com, microsoft.com, umn.edu)
            query = inLine.nextLine(); //Lưu trữ DNS mà người dùng nhập
            if (query.equalsIgnoreCase("q") || query.equalsIgnoreCase("quit")) {
                sendOut.println("hangup");//tell the server to disconnect
                sendOut.close();
                readIn.close();
                cSock.close();
                System.exit(0);
            }
            else{
                // System.out.println(query);
                sendOut.println(query);
                //Read in the returned information
                String data = readIn.readLine();
                //close all open Objects
                //print query information.
                System.out.println("Received: '" + data + "'\n");
            }
        }
    }
}
