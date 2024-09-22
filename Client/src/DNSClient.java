import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class DNSClient {
    private JFrame frame;
    private JTextField queryField;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JButton queryButton;
    private JButton quitButton;
    private Socket cSock;
    private PrintWriter sendOut;
    private BufferedReader readIn;

    public DNSClient() {
        initialize();
        connectToServer();
    }

    private void initialize() {
        frame = new JFrame("DNS Client");
        frame.setBounds(100, 100, 600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(240, 255, 240));

        queryField = new JTextField();
        queryField.setBackground(new Color(255, 255, 224));
        queryField.setFont(new Font("Arial", Font.PLAIN, 14));
        queryField.setForeground(Color.BLACK);
        queryField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        frame.getContentPane().add(queryField, BorderLayout.NORTH);
        queryField.setColumns(10);

        // Tạo bảng kết quả
        String[] columnNames = {"Root / Local", "Domain Name", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0);
        resultTable = new JTable(tableModel);
        resultTable.setBackground(new Color(255, 228, 225));
        JScrollPane scrollPane = new JScrollPane(resultTable);
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Panel chứa nút
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(240, 255, 240));
        frame.getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        queryButton = new JButton("Start");
        buttonPanel.add(queryButton);

        quitButton = new JButton("Quit");
        buttonPanel.add(quitButton);

        queryButton.addActionListener(e -> sendQuery());
        quitButton.addActionListener(e -> quit());

        frame.setVisible(true);
    }

    private void connectToServer() {
        // Tạo một dialog để nhập IP
        String host = JOptionPane.showInputDialog(frame, "Enter server IP:", "localhost");
        if (host == null || host.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No IP provided, defaulting to localhost.");
            host = "localhost";
        }

        int port = 5001;
        try {
            cSock = new Socket(host, port);
            sendOut = new PrintWriter(cSock.getOutputStream(), true);
            readIn = new BufferedReader(new InputStreamReader(cSock.getInputStream()));
            JOptionPane.showMessageDialog(frame, "Connected to server at " + host + ":" + port);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Failed to connect to server at " + host + ":" + port);
        }
    }

    private void sendQuery() {
        String query = queryField.getText().trim();
        if (!query.isEmpty()) {
            try {
                sendOut.println(query);
                String response = readIn.readLine();

                // Kiểm tra nếu server trả về theo format: "Local DNS: domain.com:1.2.3.4"
                if (response.startsWith("Local DNS:") || response.startsWith("Root DNS:")) {
                    String[] parts = response.split(":\\s+|:");  // Tách chuỗi theo ": " hoặc ":"

                    // Đảm bảo đủ 3 phần: Root or Local DNS, tên miền, và địa chỉ
                    if (parts.length == 3) {
                        String type = parts[0].trim();
                        String domainName = parts[1].trim();
                        String address = parts[2].trim();

                        // Thêm kết quả vào bảng
                        tableModel.addRow(new Object[]{type, domainName, address});
                    } else {
                        JOptionPane.showMessageDialog(frame, "Invalid response format from server.");
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "Unexpected response from server.");
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "Error during query.");
            }
        }
    }

    private void quit() {
        try {
            sendOut.println("hangup");
            sendOut.close();
            readIn.close();
            cSock.close();
            System.exit(0);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error closing connection.");
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                DNSClient window = new DNSClient();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
