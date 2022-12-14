package ru.example.chat;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

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
    TextArea msgArea;

    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());

            Thread thread = new Thread(() -> {
                while (true) {
                    try {
                        String str = is.readUTF();
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
    }

    public void sendMsg() {
        try {
        os.writeUTF(msgField.getText());
        msgField.clear();
        } catch (IOException e) {
            throw  new RuntimeException("Невозможно отправить сообщение");
        }
    }
}
