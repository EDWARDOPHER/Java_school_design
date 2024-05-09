import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Client {
    private Socket socket;  // server socket
    private BufferedReader in;
    private PrintWriter out;

    private JFrame frame = new JFrame("Chat Client");
    private JTextField ipAddressField = new JTextField(20);
    private JTextField portField = new JTextField(10);
    private JTextField usernameField = new JTextField(20);
    private JButton joinButton = new JButton("Join Chat Room");
    private JButton exitButton = new JButton("Exit Chat Room");
    private JTextArea messageArea = new JTextArea(8, 40);

    public Client() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // IP Address field
        ipAddressField.setEditable(false);
        ipAddressField.setText("localhost");


        // Port field
        portField.setEditable(false);
        portField.setText("12345"); // Will be updated after connecting to server

        // Layout
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("IP Address:"));
        topPanel.add(ipAddressField);
        topPanel.add(new JLabel("Port:"));
        topPanel.add(portField);
        topPanel.add(new JLabel("Username:"));
        topPanel.add(usernameField);
        topPanel.add(joinButton);
        topPanel.add(exitButton);

        frame.getContentPane().add(topPanel, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        // Button listeners
        joinButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                joinChatRoom();
            }
        });

        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exitChatRoom();
            }
        });

        frame.setVisible(true);
    }

    private void joinChatRoom() {
        String serverAddress = ipAddressField.getText();
        int port = Integer.parseInt(portField.getText());
        String username = usernameField.getText();
        
        if (!username.isEmpty()) {
            try {
                System.out.println("serverAddr: " + serverAddress);
                socket = new Socket(serverAddress, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println(username);
                joinButton.setEnabled(false);
                exitButton.setEnabled(true);

                // Start receiving messages
                Thread receiveThread = new Thread(() -> {
                    try {
                        String line;
                        while ((line = in.readLine()) != null) {
                            messageArea.append(line + "\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                receiveThread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void exitChatRoom() {
        if (socket != null) {
            try {
                socket.close();
                joinButton.setEnabled(true);
                exitButton.setEnabled(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Client();
            }
        });
    }
}
