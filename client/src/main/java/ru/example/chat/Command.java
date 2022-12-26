package ru.example.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor

public enum Command {

    EXIT("/exit "),
    LOGIN("/login "),
    SEND_PRIVATE_MESSAGE("/private_message "),
    CHANGE_NICKNAME("/change_nickname "),
    GET_HISTORY("/get_history "),
    GET_CLIENT_LIST("/client_list "),
    SHOW_MY_NICKNAME("/who_am_i "),
    SEND_LOGIN_FAILED("/login_failed "),
    SEND_LOGIN_OK("/login_ok ");

    @Getter
    private final String description;
}
