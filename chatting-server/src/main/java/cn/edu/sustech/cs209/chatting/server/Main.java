package cn.edu.sustech.cs209.chatting.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

  public static Map<String, Socket> clientMap = new ConcurrentHashMap<String, Socket>();

  // 具体处理与每个客户端通信的内部类
  private static class ExecuteClient implements Runnable {

    private Socket client;

    public ExecuteClient(Socket client) {
      this.client = client;
    }

    @Override
    public void run() {
      try {
        System.out.println("thread running");
        Scanner in = new Scanner(client.getInputStream());
        String strFromClient;
        while (true) {
          if (in.hasNextLine()) {
            strFromClient = in.nextLine();
            System.out.println(strFromClient);
            if (strFromClient.startsWith("namelist")) {
              System.out.println("yes,hihihih");
              OutputStream os = client.getOutputStream();
              String namelists = ";";

              for (String x : clientMap.keySet()) {
                namelists += x + ";";
              }
              namelists += '\n';
              os.write(namelists.getBytes());
              System.out.println("namelist sent");
            }
            // 注册流程
            if (strFromClient.startsWith("userName")) {
              String userName = strFromClient.split("\\:")[1];
              registerUser(userName, client);
              continue;
            }
            // 群聊流程
            if (strFromClient.contains(".")) {//startsWith("G")) {
              System.out.println("the first stap");
              String msg = strFromClient;//.split("\\:")[1];
              groupChat(msg);
              continue;
            }
            // 私聊流程
            if (strFromClient.startsWith("P")) {
              privateChat(strFromClient);
            }
            // ⽤户退出
            if (strFromClient.contains("byebye")) {
              String userName = null;
              // 根据Socket找到UserName
              for (String keyName : clientMap.keySet()) {
                if (clientMap.get(keyName).equals(client)) {
                  userName = keyName;
                }
              }
              System.out.println("⽤户" + userName + "下线了!");
              clientMap.remove(userName);
              continue;
            }
          }
        }
      } catch (IOException e) {
        System.err.println("服务器通信异常，错误为 " + e);
      }
    }

    // 注册⽅法
    private void registerUser(String userName, Socket client) {
      System.out.println("⽤户姓名为: " + userName);
//            System.out.println("⽤户"+userName+"上线了！");
//            System.out.println("当前群聊⼈数为: "+(clientMap.size()+1)+"⼈");
      // 将⽤户信息保存到map中
      clientMap.put(userName, client);

    }

    // 群聊流程
    private void groupChat(String msg) {

      try {
        System.out.println("groupchat start");
        String[] ss = msg.split("\\.");
        System.out.println(msg);
//            String to=ss[ss.length];
//            System.out.println("to"+to);
        for (int i = 0; i < ss.length - 2; i++) {
          System.out.println(ss[i]);
          Socket privateSocket = clientMap.get(ss[i]);

          System.out.println("send group msg");
          OutputStream os = privateSocket.getOutputStream();
          msg += "\n";
          os.write(msg.getBytes());

        }
      } catch (IOException e) {
        System.out.println("qun聊异常，错误为" + e);
      }
    }

    // 私聊流程
    private void privateChat(String msg) {
      String[] ss = msg.split("-");
      String to = ss[2];
      System.out.println("to" + to);
      Socket privateSocket = clientMap.get(to);
      try {
        OutputStream os = privateSocket.getOutputStream();
        msg += "\n";
        os.write(msg.getBytes());
      } catch (IOException e) {
        System.err.println("私聊异常，错误为" + e);
      }
    }
  }


  public static void main(String[] args) throws IOException {
    ExecutorService executorService = Executors.newFixedThreadPool(20);
    System.out.println("Starting server");
    ServerSocket serverSocket = new ServerSocket(8089);
    System.out.println("监听端口8089");

    while (true) {
      Socket socket = serverSocket.accept();
      System.out.println("Connected");
//            ExecuteClient x=new ExecuteClient(socket);
      executorService.submit(new ExecuteClient(socket));
//            x.run();
//            new Thread(new test(socket)).start();

    }

  }

}
