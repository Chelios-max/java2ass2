package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Platform;
import cn.edu.sustech.cs209.chatting.common.Message;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import jdk.jpackage.main.Main;

public class Controller implements Initializable {

  String currentclient = "";
  @FXML
  public ListView<String> chatList;
  @FXML
  ListView<Message> chatContentList;
  @FXML
  private TextArea inputArea;
  public Map<String, List<Message>> datas = new HashMap<>();
  Socket socket;
  String username;
  public String[] names =null;
  int tot = 0;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    try {

      Dialog<String> dialog = new TextInputDialog();
      dialog.setTitle("Login");
      dialog.setHeaderText(null);
      dialog.setContentText("Username:");

      Socket socket = new Socket("localhost", 8089);
      this.socket = socket;
      OutputStream os = socket.getOutputStream();

      Optional<String> input = dialog.showAndWait();

      if (input.isPresent() && !input.get().isEmpty()) {
            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */
        username = input.get();

        os.write(("userName:" + username + '\n').getBytes());
        Thread readFromServer = new Thread(new ReadFromServerThread(socket, this));
        readFromServer.start();

      } else {
        System.out.println("Invalid username " + input + ", exiting");
        Platform.exit();
      }

      chatContentList.setCellFactory(new MessageCellFactory());
      System.out.println("ini finish");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  @FXML
  public void PrivateChat() throws IOException {

    AtomicReference<String> user = new AtomicReference<>();

    Stage stage = new Stage();
    ComboBox<String> userSel = new ComboBox<>();

    System.out.println("lengteh of names");

    OutputStream os = socket.getOutputStream();
    os.write("namelist\n".getBytes());
    System.out.println("sent success");
    int y = 0;
    for (int i = 1; i <= 10000; i++) {
      Random random = new Random();
      y += random.nextInt();
    }
    System.out.println(y);

    for (int i = 0; i < names.length; i++) {
        if (names[i] == null) {
            continue;
        }
        if (names[i].equals(username)) {
            continue;
        }
        if (names[i].equals("")) {
            continue;
        }
      userSel.getItems().add(names[i]);
    }
    Button okBtn = new Button("OK");
    okBtn.setOnAction(e -> {
      user.set(userSel.getSelectionModel().getSelectedItem());
      stage.close();
    });

    HBox box = new HBox(10);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(20, 20, 20, 20));
    box.getChildren().addAll(userSel, okBtn);
    stage.setScene(new Scene(box));
    stage.showAndWait();
    System.out.println("finifhs");

    ObservableList<String> items = chatList.getItems();
//        if(datas==null) datas=new HashMap<>();
    currentclient = user.get();
    if (items.contains(user.get())) {
      List<Message> curdata = datas.get(currentclient);
      ObservableList<Message> itemsdata = FXCollections.observableArrayList(curdata);
      chatContentList.setItems(itemsdata);
      chatContentList.getCellFactory().call(chatContentList);
    } else {
      items.add(user.get());
      chatList.setItems(items);
      List<Message> curdata = new ArrayList<>();
      datas.put(user.get(), curdata);

      ObservableList<Message> itemsdata = FXCollections.observableArrayList(curdata);
      chatContentList.setItems(itemsdata);
      chatContentList.getCellFactory().call(chatContentList);
    }

    chatList.setOnMouseClicked((EventHandler<MouseEvent>) event -> {
      if (event.getClickCount() == 2) {
        String selectedItem = chatList.getSelectionModel().getSelectedItem();
        System.out.println("Selected item: " + selectedItem);
        currentclient = selectedItem;
        List<Message> curdata = datas.get(selectedItem);
        ObservableList<Message> itemsdata = FXCollections.observableArrayList(curdata);
        chatContentList.setItems(itemsdata);
        chatContentList.getCellFactory().call(chatContentList);
      }
    });


  }

  @FXML
  public void GroupChat() throws IOException {
    AtomicReference<String> user = new AtomicReference<>();
    ArrayList<String> nameofgroup = new ArrayList<>();
    Stage stage = new Stage();
    ListView<String> userSel = new ListView<>();

    System.out.println("lengteh of names");

    OutputStream os = socket.getOutputStream();
    os.write("namelist\n".getBytes());
    System.out.println("sent success");
    int y = 0;
    for (int i = 1; i <= 100000; i++) {
      Random random = new Random();
      y += random.nextInt();
    }
    System.out.println(y);

    ObservableList<String> items = FXCollections.observableArrayList(names);
    userSel.setItems(items);

    userSel.setOnMouseClicked((EventHandler<MouseEvent>) event -> {
      if (event.getClickCount() == 2) {
        String selectedItem = userSel.getSelectionModel().getSelectedItem();
        System.out.println("Selected item: " + selectedItem);
          if (!nameofgroup.contains(selectedItem)) {
              nameofgroup.add(selectedItem);
          }
      }
    });

    Button okBtn = new Button("OK");
    okBtn.setOnAction(e -> {
      stage.close();
    });

    HBox box = new HBox(10);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(20, 20, 20, 20));
    box.getChildren().addAll(userSel, okBtn);
    stage.setScene(new Scene(box));
    stage.showAndWait();
    System.out.println("finifhs");
    String nameofchat = "";
      if (!nameofgroup.contains(username)) {
          nameofgroup.add(username);
      }

    for (int i = 0; i < nameofgroup.size(); i++) {
      nameofchat += nameofgroup.get(i) + ".";
    }
    nameofchat += username;
    nameofchat += ".";
    nameofchat += username;
    nameofchat += "-group chat started";
    nameofchat += '\n';
    os = socket.getOutputStream();
    os.write(nameofchat.getBytes());

  }

  public void receivemsg(Message msg) {
    String clienther = msg.getSentBy();
    if (!datas.containsKey(clienther)) {
      List<Message> xs11 = new ArrayList<>();
      datas.put(clienther, xs11);
      ObservableList<String> items = chatList.getItems();
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          items.add(clienther);
        }
      });

      chatList.setItems(items);
      if (currentclient.equals("")) {
        currentclient = clienther;
      }
    }
    List<Message> x = datas.get(clienther);
    x.add(msg);
    datas.remove(msg.getSentBy());
    datas.put(msg.getSentBy(), x);
    ObservableList<Message> itemsdata = FXCollections.observableArrayList(x);

    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        chatContentList.setItems(itemsdata);
        chatContentList.getCellFactory().call(chatContentList);
      }
    });

  }

  @FXML
  public void doSendMessage() throws IOException {
    System.out.println(inputArea.getText());
    if (currentclient.contains(".")) {
      System.out.println("send");
      String send = currentclient + username + "." + username + "-" + inputArea.getText() + '\n';
      OutputStream os = socket.getOutputStream();
      os.write(send.getBytes());
      inputArea.clear();
    } else {
      String send = "P-" + username + "-" + currentclient + "-" + inputArea.getText() + '\n';
      OutputStream os = socket.getOutputStream();
      os.write(send.getBytes());

      List<Message> x = datas.get(currentclient);
      datas.remove(currentclient);
      Message msg = new Message(0L, username, currentclient, inputArea.getText());
      x.add(msg);
      datas.put(currentclient, x);
      ObservableList<Message> itemsdata = FXCollections.observableArrayList(x);
      chatContentList.setItems(itemsdata);
      chatContentList.getCellFactory().call(chatContentList);
      inputArea.clear();
    }

  }

  private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {

    @Override
    public ListCell<Message> call(ListView<Message> param) {
      return new ListCell<Message>() {

        @Override
        public void updateItem(Message msg, boolean empty) {
          super.updateItem(msg, empty);
          if (empty || Objects.isNull(msg)) {
            setText(null);
            setGraphic(null);
            return;
          }

          HBox wrapper = new HBox();
          Label nameLabel = new Label(msg.getSentBy());
          nameLabel.setText(msg.getSentBy());
          Label msgLabel = new Label(msg.getData());
          String[] ss = msg.getSentBy().split("\\.");
          if (msg.getSentBy().contains(".")) {
            System.out.println("wwwwwwwwwwwwwwwwww");

            ss = msg.getSentBy().split("\\.");
            String[] hh = msg.getData().split("-");
            nameLabel.setText(hh[0]);
            msgLabel.setText(hh[1]);
          }

          nameLabel.setPrefSize(50, 20);
          nameLabel.setWrapText(true);
          nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

          if (username.equals(nameLabel.getText())) {
            wrapper.setAlignment(Pos.TOP_RIGHT);
            wrapper.getChildren().addAll(msgLabel, nameLabel);
            msgLabel.setPadding(new Insets(0, 20, 0, 0));
          } else {
            wrapper.setAlignment(Pos.TOP_LEFT);
            wrapper.getChildren().addAll(nameLabel, msgLabel);
            msgLabel.setPadding(new Insets(0, 0, 0, 20));
          }

          setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          setGraphic(wrapper);
        }
      };
    }
  }
}

class ReadFromServerThread implements Runnable {

  private Socket client;
  Controller controller;

  public ReadFromServerThread(Socket client, Controller controller) {
    this.client = client;
    this.controller = controller;
  }

  @Override
  public void run() {
    try {
      //获取客户端输入流
      Scanner in = new Scanner(client.getInputStream());
      in.useDelimiter("\n");
      while (true) {
        if (in.hasNext()) {
          String ms = in.next();
          System.out.println("message follows---------");
          System.out.println(ms);
          if (ms.startsWith("P")) {
            String[] ss = ms.split("-");
            Message msg = new Message(0L, ss[1], ss[2], ss[3]);
            controller.receivemsg(msg);
            System.out.println("receive success");
          }
          if (ms.startsWith(";")) {
            System.out.println("namegeting");
            controller.names = ms.split(";");
            controller.names[0] = "";
          }
          if (ms.contains(".")) {//qunliao
            System.out.println("receive qunliao success");
            String[] ss = ms.split("\\.");
            String gropname = "";
            for (int i = 0; i < ss.length - 2; i++) {
              gropname += ss[i] + ".";
            }
            Message msg = new Message(0L, gropname, controller.username, ss[ss.length - 1]);
            controller.receivemsg(msg);
            System.out.println("receive qunliao success");
          }


        }
//                if(client.isClosed()){
//                    System.out.println("客户端已经关闭");
//                    break;
//                }
      }
//            in.close();
    } catch (IOException e) {
      System.err.println("客户端读线程异常，错误为 " + e);
    }
  }
}
