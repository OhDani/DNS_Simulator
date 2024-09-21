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

    // Hàm tra cứu địa chỉ IP và tên miền
    private String ipLookup(String address) {
        InetAddress[] inetAddresses;
        StringBuilder result = new StringBuilder();

        try {
            // Lấy tất cả các địa chỉ IP hoặc tên miền tương ứng
            inetAddresses = InetAddress.getAllByName(address);
        } catch (Exception e) {
            return "Host not found";
        }

        for (InetAddress inetAddress : inetAddresses) {
            String hostName = inetAddress.getCanonicalHostName();
            String hostAddress = inetAddress.getHostAddress();
            String entry = hostName + ":" + hostAddress;
            result.append(entry).append("\n");

            String cachedResult = cacheReader(hostName, hostAddress);
            if (cachedResult == null) {
                // Nếu chưa có trong cache thì thêm vào
                cacheGenerator(entry);
            } else {
                result.append("Cached Result: ").append(cachedResult).append("\n");
            }
        }
        return result.toString().trim();
    }

    // Ghi kết quả tra cứu vào cache (có thể nhiều IP hoặc tên miền)
    private void cacheGenerator(String inetAddressResult) {
        try {
            FileWriter fw = new FileWriter(CACHE_FILE_NAME, true); // Append vào file
            fw.write(inetAddressResult + "\n");
            fw.close();
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    // Đọc cache để kiểm tra xem tên miền hoặc IP đã được lưu chưa
    private String cacheReader(String hostName, String hostAddress) {
        BufferedReader reader;
        String line;
        String hostPattern = ".*(" + hostName + "|" + hostAddress + ")\\:";
        Pattern p = Pattern.compile(hostPattern);
        Matcher m;

        try {
            reader = new BufferedReader(new FileReader(CACHE_FILE_NAME));
            while ((line = reader.readLine()) != null) {
                m = p.matcher(line);
                if (m.find()) {
                    // Tìm thấy tên miền hoặc IP trong cache
                    return line;
                }
            }
        } catch (FileNotFoundException e) {
            // Nếu không tìm thấy file cache, tạo file mới trong cacheGenerator
            System.out.println("- Cache File is generated.");
            return null;
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
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
            String userOutput;

            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equals("hangup")) {
                    // Đóng kết nối khi nhận được tín hiệu "hangup"
                    clientSocket.close();
                    in.close();
                    out.close();
                    System.out.printf(">> User %d disconnected.\n", userId);
                    break;
                }

                // Thực hiện tra cứu tên miền hoặc IP
                userOutput = ipLookup(inputLine);
                out.println(userOutput);

                System.out.printf("Server to user %d: %s\n", userId, userOutput);
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }
}
