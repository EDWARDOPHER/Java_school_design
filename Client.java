import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Chat Client");
    private JTextField ipAddressField = new JTextField(20);
    private JTextField portField = new JTextField(10);
    private JTextField usernameField = new JTextField(20);
    private JButton joinButton = new JButton("Join Chat Room");
    private JButton exitButton = new JButton("Exit Chat Room");
    private JTextArea chatArea = new JTextArea(15, 40);
    private JTextArea inputArea = new JTextArea(3, 30);
    private JButton sendButton = new JButton("Send");

    private String username;

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

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JScrollPane(inputArea));
        inputPanel.add(sendButton);
        centerPanel.add(inputPanel, BorderLayout.SOUTH);
        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);

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

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        frame.pack();
        frame.setVisible(true);
    }

    private void joinChatRoom() {
        String serverAddress = ipAddressField.getText();
        int port = Integer.parseInt(portField.getText());
        username = usernameField.getText();
        
        if (!username.isEmpty()) {
            try {
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
                            chatArea.append(line + "\n");
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
                chatArea.append(username + " has exited the char.\n");
                socket.close();
                joinButton.setEnabled(true);
                exitButton.setEnabled(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage() {
        if (out != null) {
            String message = inputArea.getText();
            out.println(message);
            inputArea.setText("");
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
