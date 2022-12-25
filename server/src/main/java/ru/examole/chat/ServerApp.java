package ru.examole.chat;

public class ServerApp {
    //todo записывать в файл историю чата и при подключении отправлять пользователю
    //todo ----> на будущее кажется логично вынести логики с именем клиентХэндлера в БД ?
    public static void main(String[] args) {
        new Server(8189);
    }
}
