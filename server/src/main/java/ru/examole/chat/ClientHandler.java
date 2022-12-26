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
    private String nickname;

    public String getNickname() {
        return nickname;
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
                    server.broadcast(nickname, msg);
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
        System.out.println("Сеанс подключения с клиентом: " + nickname + " завершён ");
    }

    public void sentMassage(String msg) {
        try {
            os.writeUTF(msg);
        } catch (IOException e) {
            disconnect();
        }
    }


    //todo все команды вынести в енамки, реализовать паттерн команда, что бы это не значило)
    //todo порефачить в отношении массива строк чтобы уменьшить кол-во переменных
    private boolean executeCommand(String msg) throws IOException {
        System.out.println("Цикл получения служебных сообщений");
        String command = msg.split("\\s+")[0];

        if (command.equals("/exit")) {
            return false;
        }
        if (command.equals("/ch")) {
            String[] tokens = msg.split("\\s+");
            if (!checkCommandLength(tokens,2)){
                return true;
            }

            String newNickname = tokens[1];
            if (server.changeNickname(nickname, newNickname)) {
                this.nickname = newNickname;
                server.updateUserList();
                sentMassage("Ваш ник: " + nickname);
            } else {
                sentMassage("Ник " + newNickname + "занят");
            }
            return true;
        }

        if (command.equals("/who_am_i")) {
            sentMassage("Ваш ник: " + nickname);
            return true;
        }

        if (command.equals("/private")) {
            String[] tokens = msg.split("\\s+", 3);
            server.sentPrivateMessage(this, tokens[1], tokens[2]);
            return true;
        }

        if (command.equals("/login")) {

            String[] tokens = msg.split("\\s+");

            if (!checkCommandLength(tokens, 3)) {
                return true;
            }

            String nickname = server.getNicknameByLoginAndPassword(tokens[1], tokens[2]);
            if (nickname == null) {
                sentMassage("/login_failed " + "Некорректный логин или пароль");
                return true;
            }

            sentMassage("/login_ok " + nickname);
            this.nickname = nickname;
            server.subscribe(this);
            sentMassage(server.getHistory());
            return false;
        }
        return true;
    }

    private boolean checkCommandLength(String[] tokens, int normalLength) {
        if (tokens.length > normalLength) {
            sentMassage("Слишком много знаков в команде");
            return false;
        }
        return true;
    }
}
