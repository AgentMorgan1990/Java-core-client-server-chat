package ru.examole.chat;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {

    private Socket socket;
    private Server server;
    private DataInputStream is;
    private DataOutputStream os;
    @Getter
    private String nickname;
    private static final Logger log = LogManager.getLogger(ClientHandler.class);

    public ClientHandler(Server server, Socket socket) throws IOException {

        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        this.socket = socket;
        this.server = server;

        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    String msg = is.readUTF();

                    if (msg.equals(Command.COMMAND_EXIT.toString())) {
                        log.debug("Вошли в выполнение команды выхода");
                        break;
                    }

                    if (msg.startsWith("COMMAND")) {
                        log.debug("Вошли в выполнение других команд");
                        executeCommand(msg);
                        continue;
                    }

                    server.broadcast(nickname, msg);
                    log.debug("Рассылка сообщения");
                }
            } catch (IOException e) {
                log.error(e);
            } finally {
                disconnect();
            }
        });
        thread.start();
    }

    private void disconnect() {
        server.unsubscribe(this);
        sentMassage(Command.COMMAND_EXIT.toString());
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Сеанс подключения с клиентом: " + nickname + " завершён ");
    }

    public void sentMassage(String msg) {
        try {
            os.writeUTF(msg);
        } catch (IOException e) {
            disconnect();
        }
    }

    private void executeCommand(String msg) throws IOException {
        log.debug("Полученная команда " + msg);
        Command command = Command.valueOf(msg.split("\\s+")[0]);

        switch (command) {
            case COMMAND_CHANGE_NICKNAME:
                changeNickname(msg);
                break;
            case COMMAND_SHOW_MY_NICKNAME:
                showNickname();
                break;
            case COMMAND_SEND_PRIVATE_MESSAGE:
                sendPrivateMessage(msg);
                break;
            case COMMAND_LOGIN:
                tryToLogin(msg);
                break;
        }
    }

    private void changeNickname(String msg) {

        String[] tokens = msg.split("\\s+");
        if (!checkCommandLength(tokens, 2)) {
            sentMassage("Некорректная длина комманды при смене ника");
            return;
        }

        String newNickname = tokens[1];
        if (server.changeNickname(nickname, newNickname)) {
            this.nickname = newNickname;
            server.updateUserList();
            sentMassage("Ваш ник: " + nickname);
        } else {
            sentMassage("Ник " + newNickname + "занят");
        }
    }

    private void showNickname() {
        sentMassage("Ваш ник: " + nickname);
    }

    private void sendPrivateMessage(String msg) {
        String[] tokens = msg.split("\\s+", 3);
        server.sentPrivateMessage(this, tokens[1], tokens[2]);
    }


    private void tryToLogin(String msg) {
        String[] tokens = msg.split("\\s+");

        if (!checkCommandLength(tokens, 3)) {
            sentMassage(Command.COMMAND_SEND_LOGIN_FAILED + " Некорректная длина логина или пароля");
            return;
        }

        String nickname = server.getNicknameByLoginAndPassword(tokens[1], tokens[2]);
        if (nickname == null) {
            sentMassage(Command.COMMAND_SEND_LOGIN_FAILED + " Некорректный логин или пароль");
            return;
        }

        sentMassage(Command.COMMAND_SEND_LOGIN_OK + " " + nickname);
        this.nickname = nickname;
        server.subscribe(this);

        //todo поправить отправку истории
        //sentMassage(server.getHistory());
    }

    private boolean checkCommandLength(String[] tokens, int normalLength) {
        if (tokens.length > normalLength) {
            sentMassage("Слишком много знаков в команде");
            return false;
        }
        return true;
    }
}
