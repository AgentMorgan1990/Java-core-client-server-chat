package ru.examole.chat;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApp {

    public static void main(String[] args) {
        //todo вынести проперти порт
        try (ServerSocket serverSocket = new ServerSocket(8189)) {
            System.out.println("Server started");
            Socket socket = serverSocket.accept();
            System.out.println("Connect");
            DataInputStream is = new DataInputStream(socket.getInputStream());
            DataOutputStream os = new DataOutputStream(socket.getOutputStream());

            while (true) {
                String msg = is.readUTF();
                System.out.println(msg);
                os.writeUTF("Echo:" + msg);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
