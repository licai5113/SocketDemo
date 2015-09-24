import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 服务器端
 * 多线程监听多个客户端
 * 收到消息后转发给所有客户端
 */

public class SocketServer {

    // 服务器端
    private ServerSocket serverSocket;

    // 监听端口
    private int port = 9988;

    // 所有客户端
    private List<Socket> sockets;

    // 退出指令字符串
    private static final String QUIT = "#quit";

    // singleton
    private static SocketServer socketServer = new SocketServer();

    // 初始化服务器
    private SocketServer() {
        try {
            this.serverSocket = new ServerSocket(port);
            this.sockets = Collections.synchronizedList(new ArrayList<>());
            System.out.println("--------------server start succeed!---------");
        } catch (IOException e) {
            System.out.println("--------------server start failed!----------");
            e.printStackTrace();
        }
    }

    public static SocketServer getInstance() {
        return socketServer;
    }

    /**
     * 服务启动
     */
    public void start() {
        try {
            while (true) {
                // accept()将阻塞线程
                Socket socket = serverSocket.accept();

                // 保存连接
                sockets.add(socket);

                // 启用新线程处理
                new Thread(new HandlerConnection(socket, sockets)).start();
            }
        } catch (Exception e) {
            System.out.println("connect client failed:" + e.toString());
        }
    }

    /**
     * 消息处理类
     */
    private class HandlerConnection implements Runnable {

        private Socket socket;
        private List<Socket> sockets;

        public HandlerConnection(Socket socket, List<Socket> sockets) {
            this.socket = socket;
            this.sockets = sockets;
        }

        @Override
        public void run() {
            ConnectToClient();
        }

        /**
         * 客户端消息处理
         */
        private void ConnectToClient() {
            // 输入输出流
            BufferedReader reader;
            PrintWriter writer;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true/*auto flush*/);

                writer.println("Welcome, enter [#quit] to exist!");
                while (true) {
                    String msg = reader.readLine();
                    // 意思下
                    if (msg.endsWith(QUIT)) {
                        writer.println("Goodbye!");
                    }

                    // 控制台记录收到的消息
                    System.out.println("Receive message:[" + msg + "]");

                    // 向所有客户端转发收到的消息
                    sendToClients(msg);
                }
            } catch (Exception e) {
                System.out.println("[ConnectToClient]" + e.toString());
            }
        }

        /**
         * 转发消息到所有客户端
         */
        private void sendToClients(String message) {
            PrintWriter printWriter;
            try {
                for (Socket socket : sockets) {
                    printWriter = new PrintWriter(socket.getOutputStream(), true);
                    printWriter.println(message);
                }
            } catch (Exception e) {
                System.out.println("[sendToClients]" + e.toString());
            }
        }
    }

    public static void main(String[] args) {
        // 启动服务端
        SocketServer.getInstance().start();
    }
}
