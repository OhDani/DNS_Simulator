import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class dnsQuery extends Thread {
    private Socket clientSocket = null;
    private int userId;
    private static final String CACHE_FILE_NAME = "DNS_MAPPING.txt";

    dnsQuery(Socket sSock, int id) {
        this.clientSocket = sSock;
        this.userId = id;
    }

    private String ipLookup(String address) {
        InetAddress inetAddress =null;
        String hostName;
        String hostAddress;
        String result;
        try {
            inetAddress = InetAddress.getByName(address);
        } catch (Exception e) {
            // System.err.println("Exception: " + e.getMessage());
            return "Host not found";
        }

        if (inetAddress != null) {

            hostName = inetAddress.getHostName();
            hostAddress = inetAddress.getHostAddress();
            result = hostName + ":" + hostAddress;

            String cachedResult;
            if ((cachedResult = cacheReader(hostName)) == null) {
                // if hostName is not cached, cache it!
                cacheGenerator(result);
                return "Root DNS: " + result;
            } else {
                return "Local DNS: " + cachedResult;
            }
        }
        else {
            return "Host not found";
        }
    }

    private void cacheGenerator(String inetAddressResult) {
        try
        {
            FileWriter fw = new FileWriter(CACHE_FILE_NAME,true); //true: append new addresses to file
            fw.write(inetAddressResult + "\n");
            fw.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    private String cacheReader(String hostName) {

        BufferedReader reader;
        String line;
        String hostPattern = "^(" + hostName + ")\\:";
        Pattern p = Pattern.compile(hostPattern);
        Matcher m;

        try {
            FileReader fileReader = new FileReader(CACHE_FILE_NAME);
            reader = new BufferedReader(fileReader);

            while((line = reader.readLine()) != null) {
                m = p.matcher(line);
                if (m.find()) {
                    // we have the host name in our cache
                    return line;
                }
            }
        }
        catch (FileNotFoundException e) {
            // if cache file doesn't exist then create one in cacheGenerator
            System.out.println("- Cache File is generated.");
            return null;
        }
        catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            return null;
        }

        return null;
    }

    @Override
    public void run(){
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(),
                    true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader( clientSocket.getInputStream()));

            String inputLine;
            String userOutput = null;

            while ((inputLine = in.readLine()) != null)
            {
                if (inputLine.equals("hangup")) {
                    // closing the client socket - in/out stream
                    clientSocket.close();
                    in.close();
                    out.close();
                    System.out.printf(">> User %d disconnected.\n", userId);
                    break;
                }

                userOutput = ipLookup(inputLine);
                out.println(userOutput);

                System.out.printf("Server to user %d: %s\n", userId, userOutput);
            }
        } catch(Exception e){
            System.err.println("Exception: " + e.getMessage());
        }
    }
}