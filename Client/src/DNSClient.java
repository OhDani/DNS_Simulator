import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

import static jdk.jfr.internal.instrument.JDKEvents.initialize;

public class DNSClient{
    private JFrame frame;
    private JTextField queryField;
    private JTextArea resultArea;
    private JButton queryButton;
    private JButton quitButton;
    private Socket cSock;
    private PrintWriter sendOut;
    private BufferedReader readIn;
    public DNSClient(){
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

        // Khu vực hiển thị kết quả
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Arial", Font.PLAIN, 14));
        resultArea.setBackground(new Color(255, 228, 225));
        resultArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane scrollPane = new JScrollPane(resultArea);
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
            resultArea.append("No IP provided, defaulting to localhost.\n");
            host = "localhost"; // Nếu người dùng không nhập gì thì mặc định là localhost
        }

        int port = 5001; // Có thể cho phép nhập cả port nếu cần
        try {
            cSock = new Socket(host, port);
            sendOut = new PrintWriter(cSock.getOutputStream(), true);
            readIn = new BufferedReader(new InputStreamReader(cSock.getInputStream()));
            resultArea.append("Connected to server at " + host + ":" + port + "\n");
        } catch (IOException e) {
            resultArea.append("Failed to connect to server at " + host + ":" + port + ".\n");
        }
    }
    private void sendQuery() {
        String query = queryField.getText().trim();
        if (!query.isEmpty()) {
            try {
                sendOut.println(query);
                String response = readIn.readLine();
                resultArea.append("Response: " + response + "\n");
            } catch (IOException e) {
                resultArea.append("Error during query.\n");
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
            resultArea.append("Error closing connection.\n");
        }
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    DNSClient window = new DNSClient();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
