import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DNSServer {
	private JFrame frame;
	private JTextArea logArea;
	private DefaultListModel<String> userListModel;
	private JList<String> userList;
	private ServerSocket serverSocket;
	private boolean isRunning = true;
	private int idNumber = 1;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new DNSServer().createAndShowGUI();
		});
	}

	// Phương thức tạo GUI
	public void createAndShowGUI() {
		frame = new JFrame("DNS Server");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500);
		frame.setLayout(new BorderLayout());

		frame.getContentPane().setBackground(new Color(240, 248, 255));

		logArea = new JTextArea();
		logArea.setEditable(false);
		logArea.setFont(new Font("Arial", Font.PLAIN, 14));
		JScrollPane scrollPane = new JScrollPane(logArea);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Logs"));

		userListModel = new DefaultListModel<>();
		userList = new JList<>(userListModel);
		userList.setFont(new Font("Arial", Font.PLAIN, 14));
		userList.setBackground(new Color(255, 228, 225));
		JScrollPane userScrollPane = new JScrollPane(userList);
		userScrollPane.setBorder(BorderFactory.createTitledBorder("Connected Users"));

		// Sử dụng JSplitPane để chia tỷ lệ
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, userScrollPane);
		splitPane.setDividerLocation(600);
		splitPane.setResizeWeight(0.75);

		frame.add(splitPane, BorderLayout.CENTER);
		frame.setVisible(true);

		startServer();
	}

	private void startServer() {
		try {
			serverSocket = new ServerSocket(5001);
			logArea.append("Server started and listening on port 5001...\n");

			new Thread(() -> {
				while (isRunning) {
					try {
						Socket clientSocket = serverSocket.accept();
						String userInfo = "User " + idNumber + " connected.";
						logArea.append(userInfo + "\n");
						userListModel.addElement("User " + idNumber);

						// Khởi động xử lý truy vấn DNS trong một luồng mới
						new dnsQuery(clientSocket, idNumber, logArea, userListModel).start();
						idNumber++;
					} catch (IOException e) {
						logArea.append("Server error: " + e.getMessage() + "\n");
					}
				}
			}).start();
		} catch (IOException e) {
			logArea.append("Could not start server: " + e.getMessage() + "\n");
		}
	}
}
