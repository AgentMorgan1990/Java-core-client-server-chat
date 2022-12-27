package ru.examole.chat;

import lombok.AllArgsConstructor;

@AllArgsConstructor

public enum Command {

    COMMAND_EXIT,
    COMMAND_LOGIN,
    COMMAND_SEND_PRIVATE_MESSAGE,
    COMMAND_CHANGE_NICKNAME,
    COMMAND_GET_HISTORY,
    COMMAND_GET_CLIENT_LIST,
    COMMAND_SHOW_MY_NICKNAME,
    COMMAND_SEND_LOGIN_FAILED,
    COMMAND_SEND_LOGIN_OK

}
