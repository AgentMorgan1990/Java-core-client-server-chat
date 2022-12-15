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

                while (true) {
                    String msg = is.readUTF();
                    System.out.println("Цикл авторизации: " + msg);
                    if (msg.startsWith("/")) {
                        String command = msg.split(" ")[0];

                        if (command.equals("/login")) {
                            String username = msg.split(" ")[1];
                            if (server.isUsernameBusy(username)) {
                                sentControlMessage("/login_failed " + "Такой никнейм уже существует");
                                continue;
                            }
                            sentControlMessage("/login_ok " + username);
                            server.subscribe(this);
                            this.username = username;
                            break;
                        }
                    }
                }

                while (true) {
                    String msg = is.readUTF();
                    System.out.println("Цикл получения сообщений: " + msg);
                    if (msg.startsWith("/")) {
                        System.out.println("Цикл получения служебных сообщений");
                        String command = msg.split(" ")[0];

                        if (command.equals("/exit")) {
                            disconnect();
                            break;
                        }

                        if (command.equals("/who_am_i")) {
                            sentControlMessage("Ваш ник: "+ username);
                            continue;
                        }

                        if (command.equals("/private")) {
                            String username = msg.split(" ")[1];
                            String privateMessage = msg.split(" ")[2];

                            System.out.println(username);
                            System.out.println(privateMessage);

                            ClientHandler clientHandler = server.getClient(username);
                            if (username != null) {
                                clientHandler.sentMassage(privateMessage);
                                continue;
                            }
                        }
                    }
                    server.broadcast(username + ": "+ msg);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void disconnect(){
        server.unsubscribe(this);
        try {
            sentControlMessage("/exit");
        } catch (IOException e){
            e.printStackTrace();
        }
        try {
        if(socket!=null) {
            socket.close();
        }} catch (IOException e){
            e.printStackTrace();
        }
        System.out.println("Сеанс подключения завершён");
    }

    public void sentMassage(String msg) throws IOException {
        os.writeUTF(msg);
    }

    public void sentControlMessage(String msg) throws IOException {
        os.writeUTF(msg);
    }

}
