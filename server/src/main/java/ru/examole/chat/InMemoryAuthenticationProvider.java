package ru.examole.chat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InMemoryAuthenticationProvider implements AuthenticationProvider {

    public class User {
        String nickname;
        String login;
        String password;

        public User(String nickname, String login, String password) {
            this.nickname = nickname;
            this.login = login;
            this.password = password;
        }
    }

    private List<User> users;

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (User user : users) {
            if (user.login.equals(login) && user.password.equals(password)) {
                return user.nickname;
            }
        }
        return null;
    }

    @Override
    public String changeNickname(String oldNickname, String newNickname) {
        for (User user : users) {
            if (user.nickname.equals(oldNickname)) {
                user.nickname = newNickname;
            }
        }
        return newNickname;
    }

    @Override
    public void init() {
        this.users = new ArrayList<>(Arrays.asList(
                new User("SuperBob", "Bob", "100"),
                new User("Teapot", "Jo", "100"),
                new User("Quo-quo", "Alan", "100")
        ));
    }

    @Override
    public boolean isNickBusy(String nickname) {
        for (User user : users) {
            if (user.nickname.equals(nickname)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void shutdown() {
    }
}

