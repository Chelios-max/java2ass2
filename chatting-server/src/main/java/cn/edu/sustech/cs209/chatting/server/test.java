package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.IOException;

import java.io.InputStream;


import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

public class test implements Runnable {

  Socket csocket;


  test(Socket csocket) {
    this.csocket = csocket;
  }
  public void run() {

    try {
      System.out.println("服务器端 socket = " + csocket.getClass());

      InputStream ins=csocket.getInputStream();
      byte[] buf=new byte[1024];
      int readlen=0;
      Message.numofuser++;
      readlen=ins.read(buf);
      Message.usernames[Message.numofuser]=new String(buf,0,readlen);
      ins.close();
      OutputStream ous=csocket.getOutputStream();
      System.out.println(Message.usernames[Message.numofuser]);

      for(int i=1;i<=Message.numofuser;i++){
        System.out.println("name:");
        ous.write(Message.usernames[i].getBytes());
        System.out.println(Message.usernames[i]);
      }
      ins.close();
      ous.close();
      csocket.close();
    }
    catch (IOException e) {

      System.out.println(e);

    }

  }

}
