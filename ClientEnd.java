import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        String hostname = "localhost";
        int port = 12345;

        try (
            Socket socket = new Socket(hostname, port);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("已连接到服务器 " + hostname + ":" + port);

            // readLine() 会阻塞等待服务器响应
            String serverResponse = in.readLine();
            System.out.println("服务器: " + serverResponse);

            // 创建线程异步接收服务器消息
            Thread receiveThread = new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println("服务器: " + response);
                    }
                } catch (IOException e) {
                    System.out.println("与服务器连接断开");
                }
            });
            receiveThread.start();

            String userInput;
            while (true) {
                System.out.print("请输入消息: ");
                userInput = scanner.nextLine();

                // 发送消息到服务器
                out.println(userInput);

                if ("bye".equalsIgnoreCase(userInput)) {
                    break;
                }
            }

            receiveThread.join(1000); // 等待接收线程结束
        } catch (UnknownHostException e) {
            System.err.println("未知主机: " + hostname);
        } catch (IOException e) {
            System.err.println("连接服务器失败: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}