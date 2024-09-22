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
        InetAddress inetAddress;
        String hostName;
        String hostAddress;
        String result;

        try {
            inetAddress = InetAddress.getByName(address);
        } catch (Exception e) {
            return "Host not found";
        }

        if (inetAddress != null) {
            // Lấy hostName và hostAddress từ InetAddress
            hostName = inetAddress.getHostName();
            hostAddress = inetAddress.getHostAddress();
            result = hostName + ":" + hostAddress;

            // Kiểm tra xem hostName hoặc address có trong cache không
            String[] cachedResult = cacheReader(hostName, address);
            if (cachedResult == null) {
                // Nếu không có trong cache, ghi vào cache
                cacheGenerator(result);
                return "Root DNS: " + result;
            } else {
                // Nếu có trong cache, trả về kết quả từ cache
                return "Local DNS: " + cachedResult[0] + ":" + cachedResult[1];
            }
        } else {
            return "Host not found";
        }
    }
//    private String reverseLookup(String address) {
//        try {
//            InetAddress inetAddress = InetAddress.getByName(address);
//            String hostName = inetAddress.getHostName();
//            String resultIP = address + ":" + hostName;
//
//            // Kiểm tra cache
//            String cachedResultIP = cacheReader(hostName, address); // Tra cứu theo IP
//            if (cachedResultIP == null) {
//                // Nếu không có trong cache, lưu vào cache
//                cacheGenerator(resultIP); // Lưu theo định dạng IP:hostname
//                return "Root DNS: " + resultIP;
//            } else {
//                return "Local DNS: " + cachedResultIP;
//            }
//        } catch (Exception e) {
//            return "IP not found";
//        }
//    }


    private void cacheGenerator(String inetAddressResult) {
        try {
            FileWriter fw = new FileWriter(CACHE_FILE_NAME, true); //true: append new addresses to file
            fw.write(inetAddressResult + "\n");
            fw.close();
        } catch (IOException ioe) {
            logArea.append("IOException: " + ioe.getMessage() + "\n");
        }
    }

    private String[] cacheReader(String hostName, String address) {
        BufferedReader reader;
        String line;
        String hostPattern = "^(" + hostName + "|" + address + ")\\:";
        Pattern p = Pattern.compile(hostPattern);
        Matcher m;

        try {
            reader = new BufferedReader(new FileReader(CACHE_FILE_NAME));

            while ((line = reader.readLine()) != null) {
                m = p.matcher(line);
                if (m.find()) {
                    // Tách hostName và address ra
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        return parts; // Trả về mảng chứa hostName và address
                    }
                }
            }
        } catch (FileNotFoundException e) {
            logArea.append("- Cache file does not exist. Generating new cache file.\n");
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
//                if (inputLine.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) {
//                    userOutput = reverseLookup(inputLine); // Nếu là IP
//                } else {
//                    userOutput = ipLookup(inputLine); // Nếu là domain name
//                }

                userOutput = ipLookup(inputLine);
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
