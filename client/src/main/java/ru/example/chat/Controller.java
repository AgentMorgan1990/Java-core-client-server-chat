package ru.example.chat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    TextField msgField, loginField, newNicknameField;
    @FXML
    HBox msgPanel, loginPanel;
    @FXML
    PasswordField passwordField;
    @FXML
    TextArea msgArea;
    @FXML
    ListView<String> clientsList;


    private final int port = 8189;
    private final String host = "localhost";

    private Network network;
    private String username;
    private static final Logger log = LogManager.getLogger(Controller.class);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUsername(null);
        network = new Network();

        network.setOnAuthFailedCallback(args -> {
            showAlert(args[0] + "\n");
        });

        network.setOnAuthOkCallback(args -> {
            String msg = (String) args[0];
            setUsername(msg.split("\\s")[1]);
            msgArea.clear();
        });

        network.setOnHistoryReceivedCallback(args -> {
            String msg = (String) args[0];
            String[] tokens = msg.split("\\s");
            Platform.runLater(() -> {
                clientsList.getItems().clear();
                for (int i = 1; i < tokens.length; i++) {
                    clientsList.getItems().add(tokens[i]);
                }
            });
        });

        network.setOnMessageReceivedCallback(args -> {
            String msg = (String) args[0];
            msgArea.appendText(msg + "\n");
        });

        network.setOnDisconnectCallback(args -> {
            setUsername(null);
        });
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

    public void sendDisconnect() {
        if (network.isConnected()) {
            try {
                network.sendMessage(Command.COMMAND_EXIT.toString());
                log.debug("Отправленв комманда на отключение");
            } catch (IOException e) {
                showAlert("Не удалось отправить сообщение ...");
            }
        }
    }

    public void sendMsg() {
        try {

            String msg = msgField.getText();
            if (msg.startsWith("@")){
                String[] tokens = msg.split("\\s+",3);
                network.sendMessage(Command.COMMAND_SEND_PRIVATE_MESSAGE+" "+tokens[1]+" "+tokens[2]);
                log.debug("Отправлено личное сообщение "+Command.COMMAND_SEND_PRIVATE_MESSAGE+" "+tokens[1]+" "+tokens[2]);
            } else {
                network.sendMessage(msgField.getText());
                log.debug("Отправлено сообщение "+msgField.getText());
            }
                msgField.clear();
        } catch (IOException e) {
            showAlert("Не удалось отправить сообщение ...");
        }
    }

    public void login() {
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showAlert("Нельзя в качестве имени или пароля использовать пустую строку");
        }
        if (!network.isConnected()) {
            try {
                network.connect(host, port);
            } catch (IOException e) {
                showAlert("Невозможно подключиться к серверу на порт: " + 8189);
                return;
            }
        }

        try {
            network.sendMessage(Command.COMMAND_LOGIN + " " + loginField.getText() + " " + passwordField.getText());
            log.debug("Отправлено сообщение о логировании на сервер :" + Command.COMMAND_LOGIN + " " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            showAlert("Невозможно отправить данные пользователя");
        }
    }

    private void showAlert(String alertMessage){
        Platform.runLater(()->{
            Alert alert = new Alert(Alert.AlertType.ERROR, alertMessage, ButtonType.OK);
            alert.showAndWait();
        });
    }

    public void sendNewNickname() {
        try {
            network.sendMessage(Command.COMMAND_CHANGE_NICKNAME + " " + newNicknameField.getText());
            newNicknameField.clear();
        } catch (IOException e) {
            showAlert("Невозможно отправить запрос на новый никнайм");
        }
    }
}
