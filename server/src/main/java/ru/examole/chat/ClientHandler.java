package ru.examole.chat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Socket socket;
    private Server server;
    private DataInputStream is;
    private DataOutputStream os;
    private String username;

    public String getUsername(){
        return username;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {

        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        this.socket = socket;
        this.server = server;

        Thread thread = new Thread(() -> {


            try {
                //цикл авторизации
                while (true) {
                    String msg = is.readUTF();
                    System.out.println("Цикл авторизации: " + msg);
                    if (msg.startsWith("/")) {
                        if (executeCommand(msg)) continue;
                        else break;
                    }
                }
                //цикл получения сообщений
                while (true) {
                    String msg = is.readUTF();
                    System.out.println("Цикл получения сообщений: " + msg);
                    if (msg.startsWith("/")) {
                        if (executeCommand(msg)) continue;
                        else break;
                    }
                    server.broadcast(username, msg);
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

        server.unsubscribe(this);
            sentMassage("/exit");
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Сеанс подключения с клиентом: " + username + " завершён ");
    }

    public void sentMassage(String msg) {
        try {
            os.writeUTF(msg);
        } catch (IOException e) {
            disconnect();
        }
    }


    //todo все команды вынести в енамки
    private boolean executeCommand(String msg) {
        System.out.println("Цикл получения служебных сообщений");
        String command = msg.split(" ")[0];

        if (command.equals("/exit")) {
            return false;
        }
        if (command.equals("/ch")) {
            this.username = msg.split(" ")[1];
            server.updateUserList();
            sentMassage("Ваш ник: " + username);
            return true;
        }

        if (command.equals("/who_am_i")) {
            sentMassage("Ваш ник: " + username);
            return true;
        }

        if (command.equals("/private")) {
            String[] tokens = msg.split(" ", 3);
            server.sentPrivateMessage(this, tokens[1], tokens[2]);
            return true;
        }

        if (command.equals("/login")) {
            String username = msg.split(" ")[1];
            String password = msg.split(" ")[2];

            if (!server.isCorrectPassword(username,password)){
                sentMassage("/login_failed " + "Некорректный логин или пароль");
                return true;
            }
            if (server.isUsernameBusy(username)) {
                sentMassage("/login_failed " + "Такой никнейм уже существует");
                return true;
            }
            sentMassage("/login_ok " + username);
            this.username = username;
            server.subscribe(this);
            return false;
        }
        return true;
    }


}
