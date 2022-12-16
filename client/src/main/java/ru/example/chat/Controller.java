package ru.example.chat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller {

    @FXML
    TextField msgField;
    @FXML
    HBox msgPanel, loginPanel;
    @FXML
    TextField loginField;
    @FXML
    TextArea msgArea;

    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private String username;

    private void setUsername(String username) {
        this.username = username;
        if (username != null) {
            msgPanel.setManaged(true);
            msgPanel.setVisible(true);
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
        } else {
            msgPanel.setManaged(false);
            msgPanel.setVisible(false);
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
        }
    }


    private void connect() {
        try {
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());


            Thread thread = new Thread(() -> {

                while (true) {
                    try {
                        String str = is.readUTF();
                        System.out.println(str);
                        System.out.println(str.split(" ")[0]);
                        System.out.println(str.split(" ")[1]);
                        if (str.startsWith("/")) {
                            System.out.println("Вошли в служебный цикл");
                            String command = str.split(" ")[0];
                            if (command.equals("/login_failed")) {
                                System.out.println("Вошли в фейловый цикл");
                                Platform.runLater(()->{
                                    Alert alert = new Alert(Alert.AlertType.WARNING, str.split(" ", 2)[1], ButtonType.OK);
                                    alert.showAndWait();
                                });
                                continue;
                            }
                            if (command.equals("/login_ok")) {
                                System.out.println("Вошли в авторизованный цикл");
                                setUsername(str.split(" ")[1]);
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                while (true) {
                    try {
                        String str = is.readUTF();

                        if (str.startsWith("/")){
                            String command = str.split(" ")[0];
                            if (command.equals("/exit")){
                                disconnect();
                                break;
                            }
                        }
                        msgArea.appendText(str);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
//        finally {
//            disconnect();
//        }
    }

    private void disconnect() {
        setUsername(null);
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void sendMsg() {
        try {
            os.writeUTF(msgField.getText());
            msgField.clear();
        } catch (IOException e) {
            throw new RuntimeException("Невозможно отправить сообщение");
        }
    }

    public void login() {

        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            if (loginField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Нельзя в качестве имени использовать пустую строку", ButtonType.OK);
                alert.showAndWait();
            } else {
                os.writeUTF("/login " + loginField.getText());
                loginField.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException("Невозможно отправить сообщение");
        }
    }
}
