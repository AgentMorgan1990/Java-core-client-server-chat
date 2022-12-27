package ru.example.chat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.IOException;
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


    private Network network;
    private String username;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUsername(null);
        network = new Network();

        network.setOnAuthFailedCallback(args -> {
            msgArea.appendText((String) args[0] + "\n");
        });

        network.setOnAuthOkCallback(args -> {
            String msg = (String) args[0];
            setUsername(msg.split("\\s")[1]);
            msgArea.clear();
        });

        network.setOnMessageReceivedCallback(args -> {
            String msg = (String) args[0];
            if (msg.startsWith("/")) {
                if (msg.startsWith(Command.GET_CLIENT_LIST.getDescription())) {
                    //todo тут проверить прилетает ли лист с контактами, можно вынести в отдельный колбэк
                    String[] tokens = msg.split("\\s");
                    Platform.runLater(() -> {
                        clientsList.getItems().clear();
                        for (int i = 1; i < tokens.length; i++) {
                            clientsList.getItems().add(tokens[i]);
                        }
                    });
                }
                return;
            }
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
        if (!network.isConnected()) {
            try {
                network.sendMessage(Command.EXIT.getDescription());
            } catch (IOException e) {
                showAlert("Не удалось отправить сообщение ...");
            }
        }
    }

    public void sendMsg() {
        try {
            network.sendMessage(msgField.getText());
            msgField.clear();
        } catch (IOException e) {
            showAlert("Не удалось отправить сообщение ...");
        }
    }

    public void login() {
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            showAlert("Нельзя в качестве имени или пароля использовать пустую строку");
        }
        //todo вынести порт в переменную
        if (!network.isConnected()) {
            try {
                network.connect(8189);
            } catch (IOException e) {
                showAlert("Невозможно подключиться к серверу на порт: " + 8189);
                return;
            }
        }

        try {
            network.sendMessage(Command.LOGIN.getDescription() + loginField.getText() + " " + passwordField.getText());
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
}
