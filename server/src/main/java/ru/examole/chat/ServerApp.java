package ru.examole.chat;

public class ServerApp {


    //todo обновить логгирование на клиенте
    //todo обновить и дополнить логиирование на сервере
    //todo возвращать из Бд Optional, а не null
    //todo восстановить отправку истории


    // Возможные фичи

    //todo *** Модификация отображения истории сообщений:
    // Вариаент 1: реализовать HistoryManager на клиенте, с исрользованием буффередридера и буффередрайтера, сделать append true, чтобы не перезаписывалось
    // Вариант 2: возможен более сложный вариант с сохранением истории в БД на серваке,сохраняя от кого кому были сообщения
    // добавить возможность запрашивать конкретное кол-во предыдущих сообщений, например 50

    //todo *** реализовать отображение возможных служебных команд при вызове или нажатии кнопки

    //todo *** регистрация в чате

    //todo команды сделать отдельными кнопками для клиента
    public static void main(String[] args) {
        new Server(8189);
    }
}
