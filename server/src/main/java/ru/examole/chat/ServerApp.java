package ru.examole.chat;

public class ServerApp {

    //todo проверить список коменд, добавить новые и заюзать на клиенте и на сервере
    //todo добавить логгирование, через логгер
    //todo возвращать из Бд Optional
    //todo проверка, что сообщение не начинается на "/" -> это начало служебных сообщений

    //todo реализовать вариант хистори менеджера на клиенте, с исрользованием буффередридера и буффередрайтера - 5й урок, конец занятия
    // в файлрайтере передать второй аргумент, аппенд-тру, чтобы файл не перезаписывался, а добалялись записи

    //todo *** добавить реализации консольного варианта клиента, для понимания работы колбэка
    //todo *** возможен более сложный вариант с сохранением истории в БД, сохраняя от кого кому были сообщения
    //todo *** можно реализовать команду на серваке, при которой он присылает историю с указанием кол-ва строк, которое хотим получить
    //todo *** реализовать отображение возможных служебных команд при вызове или назатии кнопки
    //todo ***** применить экзекьютер сервис на сервере, для создания потоков
    //todo *** регистрация в чате
    //todo *** на будущее кажется логично вынести логики с именем клиентХэндлера в БД ?
    //todo *** сборка проекта в аозможностью запуска на винде
    //todo команды сделать отдельными кнопками для клиента
    public static void main(String[] args) {
        new Server(8189);
    }
}
