package ru.example.chat;

import lombok.Setter;

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

    public void connect(int port) throws IOException {
        socket = new Socket("localhost", port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        if (onConnectCallback != null) {
            onConnectCallback.callback();
        }

        Thread t = new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF();
                    if (msg.startsWith(Command.SEND_LOGIN_OK.getDescription())) {
                        if (onAuthOkCallback != null) {
                            onAuthOkCallback.callback(msg);
                        }
                        break;
                    }
                    if (msg.startsWith(Command.SEND_LOGIN_FAILED.getDescription())) {
                        String cause = msg.split("\\s", 2)[1];
                        if (onAuthFailedCallback != null) {
                            onAuthFailedCallback.callback(cause);
                        }
                    }
                }
                while (true) {
                    String msg = in.readUTF();
                    if (onMessageReceivedCallback != null) {
                        onMessageReceivedCallback.callback(msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
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

//    public void tryToLogin(String login, String password) throws IOException {
//        sendMessage("/login " + login + " " + password);
//    }

    public void disconnect() {
        if (onDisconnectCallback != null) {
            onDisconnectCallback.callback();
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
