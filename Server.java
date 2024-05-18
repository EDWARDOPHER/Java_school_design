import java.io.*;   // 文件读写
import java.net.*;  // java 网络编程
import java.util.*; 
import java.time.LocalTime; // 时间
import java.time.format.DateTimeFormatter;

public class Server {
    /**
     * @param List 列表: 可以添加，获取，删除元素，可以判断元素的索引
     * 常用方法：
     * add(): 添加元素， remove(): 移除， get(): 获取元素， contains(): 是否包含某个元素
     * 
     * @param PrintWriter 往文件里写入操作
     */
    private ServerSocket serverSocket;  // 服务端
    private List<ClientHandler> clients = new ArrayList<>();
    private PrintWriter logWriter; // 日志记录
    private boolean running = true; // 服务器启动

    // 获取本地时间，并且按照hh:mm:ss的格式输出
    private LocalTime time;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    String currTime;

    public Server(int port, String logFilePath) {
        try {
            serverSocket = new ServerSocket(port);  // 给服务器分配端口号
            File logFile = new File(logFilePath);   // 查看日志文件是否存在
            if(!logFile.exists()){
                System.err.println("\n您给出的日志文件不存在，请确认路径是否正确：" + logFilePath + '\n');
                System.exit(1);
            }else
                logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)));     // 确定写入日志文件
        }catch (IOException e) {
            System.out.println("Server初始化错误");
        }
    }

    // 启动方法
    public void start() {
        System.out.println("日志文件流创建成功，可以接收系统运行日志文件了，全民大聊天正式开始");
        time = LocalTime.now();
        currTime = time.format(formatter);
        log("[" + currTime + "]" + " 系统启动...\n");
        
        // 监听用户是否想结束server
        Thread consoleEnd = new Thread(() -> {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            try {
                while (running) {
                    System.out.println("结束聊天服务器请在控制台输入指令：end");
                    String input = reader.readLine();
                    if (input.equalsIgnoreCase("end")) {
                        stopServer();
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("IOException from consoleEnd");
            }
        });
        consoleEnd.start();

        // 监听client连接
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();

                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                handler.start();
            } catch (IOException e) {
                System.out.println("IOException from start(running)");
            }
        }
    }

    // 关机方法
    private void stopServer() {
        running = false;
        try {
            System.out.println("\n聊天已终止，已与所有客户端中断连接\n");
            clients.clear();
            serverSocket.close();
            logWriter.close();
            System.exit(1);
        } catch (IOException e) {
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

    // 日志记录方法
    private void log(String message) {
        if(logWriter == null){
            System.out.println("日志文件存在问题，写入异常");
            System.exit(1);
        }
        logWriter.println(message);
        logWriter.flush();
    }

    // client类
    private class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;  // client传回的消息
        private PrintWriter out;    // 写入client的消息

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                System.out.println("IOException from ClientHadler constructor");
            }
        }

        public void run() {
            String username = "";
            try {
                username = in.readLine();
                time = LocalTime.now();
                currTime = time.format(formatter);
                
                System.out.println('[' + currTime + ']' + " 欢迎" + username + "进入聊天室！");
                broadcast('[' + currTime + ']' + " 欢迎" + username + "进入聊天室！");
                log('[' + currTime + ']' + " 欢迎" + username + "进入聊天室！");

                String message;
                while ((message = in.readLine()) != null) {
                    time = LocalTime.now();
                    currTime = time.format(formatter);
                    if(message.equals(""))
                        continue;
                    System.out.println("[" + username + " " + currTime + "] " + message);
                    broadcast("[" + username + " " + currTime + "] " + message);
                    log("[" + username + " " + currTime + "] " + message);
                }
            } catch (IOException e) {
                System.out.println("IOException error from ClientHandler.run01");
            } finally {
                try {
                    broadcast(username + " 退出了聊天室");
                    log(username + " 退出了聊天室");
                    System.out.println(username + " 退出了聊天室");
                    socket.close();
                    clients.remove(this);
                } catch (IOException e) {
                    System.out.println("IOException error from ClientHandler.run02");
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
