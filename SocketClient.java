import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class SocketClient {

    // 退出指令字符串
    private static final String QUIT = "#quit";

    public static void main(String[] args) throws Exception {

        // 建立连接
        Socket socket = new Socket("127.0.0.1", 9988);

        // 接收消息
        new Thread(new receiver(socket)).start();

        // 发送消息
        new Thread(new sender(socket)).start();
    }

    /**
     * 接收服务器消息
     */
    private static class receiver implements Runnable {
        private Socket socket;
        public receiver(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true) {
                    Thread.sleep(10);
                    System.out.println(reader.readLine());
                }
            } catch (Exception e) {
                System.out.println("[Receive message failed]" + e.toString());
            }
        }
    }

    /**
     * 向服务器发送消息
     */
    private static class sender implements Runnable {
        private Socket socket;
        public sender(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            Scanner scanner = null;
            PrintWriter printWriter = null;
            try {
                scanner = new Scanner(System.in);
                printWriter = new PrintWriter(socket.getOutputStream(), true/*auto flush*/);

                Boolean eof = false;
                while (!eof && scanner.hasNext()) {
                    String message = scanner.nextLine();
                    printWriter.println("Client-1: " + message);
                    if (QUIT.equals(message.trim())) {
                        eof = true;
                    }
                }
            } catch (IOException e) {
                System.out.println("[Send message failed]" + e.toString());
            } finally {
                // close resource
                if (null != scanner) {
                    scanner.close();
                }
                if (null != printWriter) {
                    printWriter.close();
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("[Close client socket failed]" + e.toString());
                }
            }
        }
    }
}


