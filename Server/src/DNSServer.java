/* 
** This program serves as the server of DNS query.
*/

import java.net.*;

class DNSServer {
	public static void main(String[] args) throws Exception {
		ServerSocket sSock = null;
		int idNumber = 1;
		try {
			sSock =  new ServerSocket(5001);
		} catch (Exception e) {
			System.out.println("Could not listen on port: 5001.");
			System.exit(1);
		}

		System.out.println("Server is listening...");
		new monitorQuit().start();

		while (true) {
			new dnsQuery(sSock.accept(), idNumber).start();
			System.out.printf(">> User %d connected:\n", idNumber);
			idNumber++;
		}
	}
}
