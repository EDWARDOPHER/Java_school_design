package ChatRoom;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private PrintWriter logWriter;

    public Server(int port, String logFilePath) {
        try {
            serverSocket = new ServerSocket(port);
            logWriter = new PrintWriter(new BufferedWriter(new FileWriter(logFilePath, true)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        log("Server is running...");
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                log("New client connected: " + socket);

                // Create a new thread to handle this client
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                handler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Server <logFilePath>");
            System.exit(1);
        }
        
        String logFilePath = args[0];
        int port = 12345; // Change this to desired port number
        Server server = new Server(port, logFilePath);
        server.start();
    }

    private void log(String message) {
        logWriter.println(message);
        logWriter.flush();
    }

    // Inner class to handle each client connection
    private class ClientHandler extends Thread {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                String username = in.readLine();
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
                    socket.close();
                    clients.remove(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void broadcast(String message) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }
    }
}
