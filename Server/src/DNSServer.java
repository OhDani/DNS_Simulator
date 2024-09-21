/* 
** This program serves as the server of DNS query.
*/

import java.net.*;

class DNSServer {
	public static void main(String[] args) throws Exception {
		//Create a socket object called sSock, of type ServerSocket for TCP.
		ServerSocket sSock = null;
		int idNumber = 1;	// keeping track of users since multi users can do queries to server
		try {
			// Try to open server socket 5001.
			sSock =  new ServerSocket(5001);
		} catch (Exception e) {
			System.out.println("Could not listen on port: 5001.");
			System.exit(1); // Handle exceptions.
		}

		System.out.println("Server is listening...");
		new monitorQuit().start(); // Start a new thread to monitor exit signal.

		while (true) {
			//If there is a pending client connection, start new query thread.
			new dnsQuery(sSock.accept(), idNumber).start();
			System.out.printf(">> User %d connected:\n", idNumber);
			idNumber++;
		}
	}
}
