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
    TextField loginField;
    @FXML
    PasswordField passwordField;
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
        boolean active = username != null;

        msgPanel.setManaged(active);
        msgPanel.setVisible(active);
        loginPanel.setVisible(!active);
        loginPanel.setManaged(!active);
        clientsList.setVisible(active);
        clientsList.setManaged(active);
        msgArea.setVisible(active);
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
                    System.out.println(str.split("\\s+")[0]);
                    System.out.println(str.split("\\s+")[1]);
                    if (str.startsWith("/")) {
                        System.out.println("Вошли в служебный цикл");
                        String command = str.split("\\s+")[0];
                        if (command.equals("/login_failed")) {
                            System.out.println("Вошли в фейловый цикл");
                            showAlert(str.split("\\s+", 2)[1]);
                            continue;
                        }
                        if (command.equals("/login_ok")) {
                            System.out.println("Вошли в авторизованный цикл");
                            setUsername(str.split("\\s+")[1]);
                            break;
                        }
                    }
                }

                while (true) {
                    String str = is.readUTF();
                    if (str.startsWith("/")) {
                        String command = str.split("\\s+")[0];
                        if (command.equals("/exit")) {
                            disconnect();
                            break;
                        }
                        if (command.equals("/client_list")) {
                            String[] tokens = str.split("\\s+");
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
                showAlert("Не удалось отправить сообщение ...");
            }
        }
    }


    public void sendMsg() {
        try {
            os.writeUTF(msgField.getText());
            msgField.clear();
        } catch (IOException e) {
            showAlert("Не удалось отправить сообщение ...");
        }
    }

    public void login() {

        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
                showAlert("Нельзя в качестве имени или пароля использовать пустую строку");
            } else {
                os.writeUTF("/login " + loginField.getText() + " " + passwordField.getText());
                loginField.clear();
                passwordField.clear();
            }
        } catch (IOException e) {
            showAlert("Не удалось отправить сообщение ...");
        }
    }

    private void showAlert(String alertMessage){
        Platform.runLater(()->{
            Alert alert = new Alert(Alert.AlertType.ERROR, alertMessage, ButtonType.OK);
            alert.showAndWait();
        });
    }
}
