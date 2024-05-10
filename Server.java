import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private ServerSocket serverSocket;  // socket
    private List<ClientHandler> clients = new ArrayList<>(); // 
    private PrintWriter logWriter; // 日志记录
    private boolean running = true;

    // 该类分配端口号和日志记录
    public Server(int port, String logFilePath) {
        try {
            serverSocket = new ServerSocket(port);
            logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)));
        }catch (FileNotFoundException e){
            System.err.println("file not found: " + logFilePath);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Server is running");
        log("Server is running...");
        
        // 监听用户是否想结束server
        Thread consoleInputThread = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                while (running) {
                    System.out.println("Stop server enter \'end\'");
                    String input = reader.readLine();
                    if (input.equalsIgnoreCase("end")) {
                        stopServer();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        consoleInputThread.start();

        while (running) {
            try {
                // 用户建立连接
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                log("New client connected: " + socket);

                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                handler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 用来结束server
    private void stopServer() {
        running = false;
        try {
            log("Server is shutting from keyboard!(User stop)");
            serverSocket.close();
            logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 监视输入日志格式
        if (args.length != 1) {
            System.out.println("Usage: java Server <logFilePath>");
            System.exit(1);
        }
        
        String logFilePath = args[0];
        int port = 12345; 
        Server server = new Server(port, logFilePath);
        server.start();
    }

    private void log(String message) {
        logWriter.println(message);
        logWriter.flush();
    }

    // client处理线程
    private class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;  // client传回的消息
        private PrintWriter out;    // 写入client的消息

        // 初始化操作
        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                // 
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            String username = "";
            try {
                username = in.readLine();

                broadcast(username + " has joined the chat.");
                log(username + " has joined the chat.");

                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(username + ": " + message);
                    log(username + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    broadcast(username + " has exited the chat.");
                    log(username + " has exited the chat.");
                    System.out.println("socket close");
                    socket.close();
                    clients.remove(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 广播操作， 在每个Client中都输出这个消息
        private void broadcast(String message) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }
    }
}
