import javax.swing.*;
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
    private JTextArea logArea;
    private Socket clientSocket = null;
    private int userId;
    private DefaultListModel<String> userListModel;
    private static final String CACHE_FILE_NAME = "DNS_MAPPING.txt";

    // Constructor
    dnsQuery(Socket sSock, int id, JTextArea logArea, DefaultListModel<String> userListModel) {
        this.clientSocket = sSock;
        this.userId = id;
        this.logArea = logArea;
        this.userListModel = userListModel;
    }


    private String ipLookup(String address) {
        InetAddress inetAddress = null;
        String hostName;
        String hostAddress;
        String result;
        try {
            inetAddress = InetAddress.getByName(address);
        } catch (Exception e) {
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
        } else {
            return "Host not found";
        }
    }
    private String reverseLookup(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            String hostName = inetAddress.getHostName();
            String hostAddress = inetAddress.getHostAddress();

            String result = hostAddress + ":" + hostName;

            // Kiểm tra cache
            String cachedResult = cacheReader(hostAddress);
            if (cachedResult == null) {
                // Nếu không có trong cache, lưu vào cache
                cacheGenerator(result);
                return "Root DNS: " + result;
            } else {
                return "Local DNS: " + cachedResult;
            }
        } catch (Exception e) {
            return "IP not found";
        }
    }


    private void cacheGenerator(String inetAddressResult) {
        try {
            FileWriter fw = new FileWriter(CACHE_FILE_NAME, true); //true: append new addresses to file
            fw.write(inetAddressResult + "\n");
            fw.close();
        } catch (IOException ioe) {
            logArea.append("IOException: " + ioe.getMessage() + "\n");
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

            while ((line = reader.readLine()) != null) {
                m = p.matcher(line);
                if (m.find()) {
                    return line;
                }
            }
        } catch (FileNotFoundException e) {
            logArea.append("- Cache File is generated.\n");
            return null;
        } catch (IOException e) {
            logArea.append("IOException: " + e.getMessage() + "\n");
            return null;
        }

        return null;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equals("hangup")) {
                    break;
                }

                String userOutput;
                if (inputLine.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
                    userOutput = reverseLookup(inputLine); // Nếu là IP
                } else {
                    userOutput = ipLookup(inputLine); // Nếu là domain name
                }

                out.println(userOutput);
                logArea.append("Server to user " + userId + ": " + userOutput + "\n");
            }
        } catch (IOException e) {
            logArea.append("Exception: " + e.getMessage() + "\n");
        } finally {
            try {
                // Đóng kết nối và streams sau khi ngắt kết nối
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                userListModel.removeElement("User " + userId);
            } catch (IOException e) {
                logArea.append("Error closing connection: " + e.getMessage() + "\n");
            }
            logArea.append(">> User " + userId + " connection closed.\n");
        }
    }

}
