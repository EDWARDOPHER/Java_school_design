import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Server {
    private ServerSocket serverSocket;  // socket
    private List<ClientHandler> clients = new ArrayList<>(); // 
    private PrintWriter logWriter; // 日志记录
    private boolean running = true;

    private LocalTime time;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    String currTime;

    // 该类分配端口号和日志记录
    public Server(int port, String logFilePath) {
        try {
            serverSocket = new ServerSocket(port);
            File logFile = new File(logFilePath);
            if(!logFile.exists()){
                System.err.println("\n您给出的日志文件不存在，请确认路径是否正确：" + logFilePath + '\n');
                System.exit(1);
            }else
                logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)));
        }catch (IOException e) {
            System.out.println("1");
            e.printStackTrace();
        }
    }

    // server启动方法
    public void start() {
        System.out.println("日志文件流创建成功，可以接收系统运行日志文件了，全民大聊天正式开始");
        log("日志文件流创建成功，可以接收系统运行日志文件了，全民大聊天正式开始");
        
        // 监听用户是否想结束server
        Thread consoleInputThread = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                while (running) {
                    System.out.println("结束聊天服务器请在控制台输入指令： \'end\'");
                    String input = reader.readLine();
                    if (input.equalsIgnoreCase("end")) {
                        stopServer();
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("***管理员在控制台结束了server运行***");
                log("***管理员在控制台结束了server运行***");
            }
        });
        consoleInputThread.start();

        // 监听client连接
        while (running) {
            try {
                Socket socket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                handler.start();
            } catch (IOException e) {
                System.out.println("\n***管理员在控制台结束了server运行***\n");
                log("***管理员在控制台结束了server运行***");
            }
        }
    }

    // 用来结束server
    private void stopServer() {
        running = false;
        try {

            serverSocket.close();
            logWriter.close();
        } catch (IOException e) {
            System.out.println("3");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 监视输入日志格式
        if (args.length != 1) {
            System.out.println("请输入日志文件路径！");
            System.exit(1);
        }
        
        String logFilePath = args[0];
        int port = 12345; 
        Server server = new Server(port, logFilePath);
        server.start();
    }

    private void log(String message) {
        if(logWriter == null){
            System.out.println("日志文件存在问题，写入异常");
            System.exit(1);
        }
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
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("4");
                e.printStackTrace();
            }
        }

        public void run() {
            String username = "";
            try {
                username = in.readLine();
                time = LocalTime.now();
                currTime = time.format(formatter);
                
                System.out.println(currTime + "欢迎" + username + "进入聊天室！");
                broadcast(currTime + "欢迎" + username + "进入聊天室！");
                log(currTime + "欢迎" + username + "进入聊天室！");

                String message;
                while ((message = in.readLine()) != null) {
                    time = LocalTime.now();
                    currTime = time.format(formatter);
                    System.out.println("[" + username + " " + currTime + "] " + message);
                    broadcast("[" + username + " " + currTime + "] " + message);
                    log("[" + username + " " + currTime + "] " + message);
                }
            } catch (IOException e) {
                System.out.println("5");
                e.printStackTrace();
            } finally {
                try {
                    broadcast(username + " 退出了聊天室");
                    log(username + " 退出了聊天室");
                    System.out.println(username + " 退出了聊天室");
                    socket.close();
                    clients.remove(this);
                } catch (IOException e) {
                    System.out.println("6");
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
