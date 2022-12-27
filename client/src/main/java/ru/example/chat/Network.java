package ru.example.chat;

import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    @Setter
    private Callback onAuthOkCallback;
    @Setter
    private Callback onAuthFailedCallback;
    @Setter
    private Callback onMessageReceivedCallback;
    @Setter
    private Callback onConnectCallback;
    @Setter
    private Callback onDisconnectCallback;
    @Setter
    private Callback onHistoryReceivedCallback;

    private static final Logger log = LogManager.getLogger(Network.class);


    public void connect(String host, int port) throws IOException {
        log.debug("Вызван метод connect в Network");
        socket = new Socket(host, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        if (onConnectCallback != null) {
            onConnectCallback.callback();
        }

        Thread t = new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith(Command.COMMAND_SEND_LOGIN_OK.toString())) {
                        log.info("Получен успешный ответ от сервера об авториризации "+msg);
                        if (onAuthOkCallback != null) {
                            onAuthOkCallback.callback(msg);
                        }
                        break;
                    }
                    if (msg.startsWith(Command.COMMAND_SEND_LOGIN_FAILED.toString())) {
                        String cause = msg.split("\\s", 2)[1];
                        log.info("Получен неуспешный ответ от сервера об авториризации " + cause);
                        if (onAuthFailedCallback != null) {
                            onAuthFailedCallback.callback(cause);
                        }
                    }
                }
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith(Command.COMMAND_GET_CLIENT_LIST.toString()) && onHistoryReceivedCallback != null) {
                        log.info("Получен список клиентов онлайн от сервера");
                        onHistoryReceivedCallback.callback(msg);
                        continue;
                    }
                    if (onMessageReceivedCallback != null) {
                        log.info("Получено сообщение от сервера");
                        onMessageReceivedCallback.callback(msg);
                    }
                }
            } catch (IOException e) {
                log.error(e);
            } finally {
                disconnect();
            }
        });
        t.start();
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed();
    }

    public void sendMessage(String message) throws IOException {
        out.writeUTF(message);
    }

    public void disconnect() {
        if (onDisconnectCallback != null) {
            onDisconnectCallback.callback();
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            log.error(e);
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            log.error(e);
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            log.error(e);
        }
    }
}
