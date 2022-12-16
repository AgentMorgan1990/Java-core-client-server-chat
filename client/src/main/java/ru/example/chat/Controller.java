package ru.example.chat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    TextField msgField;
    @FXML
    HBox msgPanel, loginPanel;
    @FXML
    TextField loginField,passwordField;
    @FXML
    TextArea msgArea;
    @FXML
    ListView<String> clientsList;

    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private String username;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUsername(null);
    }

    private void setUsername(String username) {
        this.username = username;
        if (username != null) {
            msgPanel.setManaged(true);
            msgPanel.setVisible(true);
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            clientsList.setVisible(true);
            clientsList.setManaged(true);
            msgArea.setVisible(true);
        } else {
            msgPanel.setManaged(false);
            msgPanel.setVisible(false);
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
            msgArea.setVisible(false);
        }
    }


    private void connect() {
        try {
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    String str = is.readUTF();
                    System.out.println(str);
                    System.out.println(str.split(" ")[0]);
                    System.out.println(str.split(" ")[1]);
                    if (str.startsWith("/")) {
                        System.out.println("Вошли в служебный цикл");
                        String command = str.split(" ")[0];
                        if (command.equals("/login_failed")) {
                            System.out.println("Вошли в фейловый цикл");
                            Platform.runLater(() -> {
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
                }

                while (true) {
                    String str = is.readUTF();
                    if (str.startsWith("/")) {
                        String command = str.split(" ")[0];
                        if (command.equals("/exit")) {
                            disconnect();
                            break;
                        }
                        if (command.equals("/client_list")) {
                            String[] tokens = str.split(" ");
                            Platform.runLater(() -> {
                                clientsList.getItems().clear();
                                for (int i = 1; i < tokens.length; i++) {
                                    clientsList.getItems().add(tokens[i]);
                                }
                            });
                            continue;
                        }
                    }
                    Platform.runLater(()->{
                        msgArea.appendText(str + "\n");
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        });
        thread.start();
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

    public void sendDisconnect() {
        if (socket != null && !socket.isClosed()) {
            try {
                os.writeUTF("/exit");
            } catch (IOException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось отправить сообщение ...", ButtonType.OK);
                    alert.showAndWait();
                });
            }
        }
    }


    public void sendMsg() {
        try {
            os.writeUTF(msgField.getText());
            msgField.clear();
        } catch (IOException e) {
            Platform.runLater(()-> {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Не удалось отправить сообщение ...", ButtonType.OK);
                alert.showAndWait();
            });
        }
    }

    public void login() {

        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            if (loginField.getText().isEmpty()||passwordField.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Нельзя в качестве имени или пароля использовать пустую строку", ButtonType.OK);
                alert.showAndWait();
            } else {
                os.writeUTF("/login " + loginField.getText()+" "+passwordField.getText());
                loginField.clear();
                passwordField.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException("Невозможно отправить сообщение");
        }
    }
}
