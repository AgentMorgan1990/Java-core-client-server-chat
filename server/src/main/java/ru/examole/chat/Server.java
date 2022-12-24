package ru.examole.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {

    private int port;
    private static List<ClientHandler> clients;
    private AuthenticationProvider authenticationProvider;

    public Server(int port) {
        this.port = port;
        this.authenticationProvider = new SQLiteAuthenticationProvider();
        clients = new ArrayList<>();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            authenticationProvider.init();

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connect");
               new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            authenticationProvider.shutdown();
        }
    }

    public synchronized void  subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        updateUserList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        updateUserList();
    }

    public synchronized void broadcast(String username, String msg) {
        for (ClientHandler client : clients) {
            client.sentMassage(username + ": " + msg);
        }
    }

    public synchronized boolean isUsernameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(username))
                return true;
        }
        return false;
    }

    public synchronized void updateUserList(){
        StringBuilder stringBuilder = new StringBuilder("/client_list ");
        for (ClientHandler client: clients) {
            stringBuilder.append(client.getNickname()).append(" ");
        }
        clients.forEach(c->c.sentMassage(stringBuilder.toString()));
    }


    public void sentPrivateMessage(ClientHandler senderClient, String recipientUsername, String message) {
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(recipientUsername)) {
                client.sentMassage("---> Получено личное сообщение от: " + senderClient.getNickname() + " для Клиента: " + recipientUsername + " " + message);
                senderClient.sentMassage("<--- Отправлено личное сообщение от: " + senderClient.getNickname() + " для Клиента: " + recipientUsername + " " + message);
                return;
            }
        }
        senderClient.sentMassage("Клиента с ником: " + recipientUsername + " нет в чате");
    }

    public String getNicknameByLoginAndPassword(String username, String password) {
        return authenticationProvider.getNicknameByLoginAndPassword(username, password);
    }

    public boolean changeNickname(String oldNickname, String newNickname) {
        if (!isUsernameBusy(newNickname)) {
            authenticationProvider.changeNickname(oldNickname, newNickname);
            return true;
        }
        return false;
    }
}

