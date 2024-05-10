import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("全民聊天室---by计科223李星原");
    private JTextField ipAddressField = new JTextField(20);
    private JTextField portField = new JTextField(10);
    private JTextField usernameField = new JTextField(20);
    private JButton joinButton = new JButton("进入聊天室");
    private JButton exitButton = new JButton("退出聊天室");
    private JTextArea chatArea = new JTextArea(15, 40);
    private JTextArea inputArea = new JTextArea(3, 30);
    private JButton sendButton = new JButton("发送");

    private String username;

    public Client() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ipAddressField.setEditable(true);
        ipAddressField.setText("localhost");

        portField.setEditable(false);
        portField.setText("12345");

        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("IP:"));
        topPanel.add(ipAddressField);
        topPanel.add(new JLabel("端口:"));
        topPanel.add(portField);
        topPanel.add(new JLabel("昵称:"));
        topPanel.add(usernameField);
        topPanel.add(joinButton);
        topPanel.add(exitButton);
        frame.getContentPane().add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatArea.setEditable(false);
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JScrollPane(inputArea));
        inputPanel.add(sendButton);
        centerPanel.add(inputPanel, BorderLayout.SOUTH);
        frame.getContentPane().add(centerPanel, BorderLayout.CENTER);

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

                JOptionPane.showMessageDialog(frame, "已经与聊天室服务器建立连接", "消息", JOptionPane.INFORMATION_MESSAGE);
                ipAddressField.setEditable(false);


                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                out.println(username);
                joinButton.setEnabled(false);
                exitButton.setEnabled(true);

                Thread receiveThread = new Thread(() -> {
                    try {
                        String line;
                        while ((line = in.readLine()) != null) {
                            if(line.equals("1")){
                                JOptionPane.showMessageDialog(frame, "与聊天室服务器失去连接", "错误", JOptionPane.ERROR_MESSAGE);
                                joinButton.setEnabled(true);
                                exitButton.setEnabled(false);
                            }
                            else
                                chatArea.append(line + "\n");
                        }
                    } catch (IOException e) {
                        // e.printStackTrace();
                    }
                });
                receiveThread.start();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(frame, "与聊天室服务器连接建立失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exitChatRoom() {
        if (socket != null) {
            try {
                chatArea.append(username + " 退出聊天室\n");
                ipAddressField.setEditable(true);
                socket.close();
                joinButton.setEnabled(true);
                exitButton.setEnabled(false);
            } catch (IOException e) {
                // e.printStackTrace();
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
