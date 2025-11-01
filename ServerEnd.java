import java.io.*;
import java.net.*;


public class Server {
    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("服务器启动，监听端口: " + port);
            System.out.println("等待客户端连接...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("客户端连接成功: " + clientSocket.getInetAddress());

                // 为每个客户端创建新线程处理
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// 客户端处理线程
class ClientHandler extends Thread {
    private Socket clientSocket;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            out.println("欢迎连接到服务器！输入 'bye' 退出");

            while ((inputLine = in.readLine()) != null) {
                System.out.println("收到客户端消息: " + inputLine);

                if ("bye".equalsIgnoreCase(inputLine)) {
                    out.println("Bye!");
                    break;
                }

                if (inputLine.equals("id")) {
                    out.println("请输入要查询的学生id");
                    if((inputLine = in.readLine()) != null) {
                        try {
                            int studentId = Integer.parseInt(inputLine);
                            ProcessBuilder pb = new ProcessBuilder("java", "QueryByStudentId", String.valueOf(studentId));
                            pb.directory(new File(System.getProperty("user.home") + "/DistributedSystemHw2"));
                            pb.redirectErrorStream(true);
                            Process process = pb.start();

                            // 读取子进程的输出
                            BufferedReader processOutput = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));
                            String line;
                            while ((line = processOutput.readLine()) != null) {
                                out.println(line);
                            }
                            process.waitFor();
                        } catch (InterruptedException e) {
                            out.println("查询被中断");
                            Thread.currentThread().interrupt();
                        } catch (NumberFormatException e) {
                            out.println("请输入有效的学号（整数）");
                        }
                    }
                }
                else if (inputLine.equals("score")) {
                    out.println("请输入要查询的最小语文成绩:");
                    String minScoreStr = in.readLine();
                    if (minScoreStr != null) {
                        out.println("请输入要查询的最大语文成绩:");
                        String maxScoreStr = in.readLine();
                        if (maxScoreStr != null) {
                            try {
                                float minScore = Float.parseFloat(minScoreStr);
                                float maxScore = Float.parseFloat(maxScoreStr);

                                if (minScore > maxScore) {
                                    out.println("错误: 最小成绩不能大于最大成绩");
                                } else {
                                    ProcessBuilder pb = new ProcessBuilder("java", "QueryByChineseScoreRange.java",
                                        String.valueOf(minScore), String.valueOf(maxScore));
                                    pb.directory(new File(System.getProperty("user.home") + "/DistributedSystemHw2"));
                                    pb.redirectErrorStream(true);
                                    Process process = pb.start();

                                    // 读取子进程的输出
                                    BufferedReader processOutput = new BufferedReader(
                                        new InputStreamReader(process.getInputStream()));
                                    String line;
                                    while ((line = processOutput.readLine()) != null) {
                                        out.println(line);
                                    }
                                    process.waitFor();
                                }
                            } catch (InterruptedException e) {
                                out.println("查询被中断");
                                Thread.currentThread().interrupt();
                            } catch (NumberFormatException e) {
                                out.println("请输入有效的分数（浮点数）");
                            }
                        }
                    }
                }
                else {
                    out.println("服务器收到: " + inputLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                System.out.println("客户端连接关闭: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}