package ru.examole.chat;

public interface AuthenticationProvider {

    String getNicknameByLoginAndPassword(String login,String password);
    String changeNickname(String oldNickname,String newNickname);
    void init();
    void shutdown();
}
