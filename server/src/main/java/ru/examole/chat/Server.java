package ru.examole.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {

    private int port;
    private static List<ClientHandler> clients;

    public Server(int port) {

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

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public void broadcast(String msg) throws IOException {
        for (ClientHandler client : clients) {
            client.sentMassage(msg);
        }
    }

    public boolean isUsernameBusy(String username) {
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username))
                return true;
        }
        return false;
    }

    public ClientHandler getClient(String username) {
        ClientHandler clientHandler = null;
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(username)) {
                clientHandler = client;
            }
        }
        return clientHandler;
    }
}

