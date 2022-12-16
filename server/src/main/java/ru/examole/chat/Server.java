package ru.examole.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Server {

    private int port;
    private static List<ClientHandler> clients;
    private static Map<String,String> authenticationMap;

    public Server(int port) {

        authenticationMap = new HashMap<>();
        authenticationMap.put("Bob","100");
        authenticationMap.put("John","200");
        authenticationMap.put("Mike","300");

        this.port = port;
        clients = new ArrayList<>();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Connect");
               new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            if (client.getUsername().equals(username))
                return true;
        }
        return false;
    }

    public synchronized void updateUserList(){
        StringBuilder stringBuilder = new StringBuilder("/client_list ");
        for (ClientHandler client: clients) {
            stringBuilder.append(client.getUsername()).append(" ");
        }
        clients.forEach(c->c.sentMassage(stringBuilder.toString()));
    }


    public void sentPrivateMessage(ClientHandler senderClient, String recipientUsername, String message) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(recipientUsername)) {
                client.sentMassage("---> Получено личное сообщение от: " + senderClient.getUsername() + " для Клиента: " + recipientUsername + " " + message);
                senderClient.sentMassage("<--- Отправлено личное сообщение от: " + senderClient.getUsername() + " для Клиента: " + recipientUsername + " " + message);
                return;
            }
        }
        senderClient.sentMassage("Клиента с ником: " + recipientUsername + " нет в чате");
    }

    public boolean isCorrectPassword(String username, String password) {

        if (!authenticationMap.containsKey(username)) {
            return false;
        }
        if (authenticationMap.get(username).equals(password)) {
            return true;
        }
        return false;

    }
}

