package ru.examole.chat;

import java.sql.ResultSet;
import java.sql.SQLException;

import static ru.examole.chat.SQLConnection.connection;

public class SQLiteAuthenticationProvider implements AuthenticationProvider {


    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        String nickname = null;
        try {
            ResultSet rs = SQLConnection.stmt.executeQuery(String.format("SELECT nickname FROM users WHERE login = '%s' AND password = '%s';", login, password));
            if (rs.next()) {
                nickname = rs.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nickname;
    }

    @Override
    public String changeNickname(String oldNickname, String newNickname) {
        try {
            SQLConnection.stmt.execute(String.format("UPDATE users SET nickname = '%s' WHERE nickname = '%s';", newNickname, oldNickname));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newNickname;
    }

    private static void dropAndCreateTable() throws SQLException {
        SQLConnection.stmt.executeUpdate("drop table if exists users;");
        SQLConnection.stmt.executeUpdate(" CREATE TABLE if not exists users (id INTEGER PRIMARY KEY AUTOINCREMENT, nickname  TEXT, login TEXT, password TEXT);");
    }

    private static void fillTable() throws SQLException {
        SQLConnection.stmt.executeUpdate("insert into users (login, nickname, password) values ('Bob','SuperBob','100'),('Alan','Wizard','100'),('Jack','DrankMan','100');");
    }

    @Override
    public void init() {
        SQLConnection.connect();
        try {
            SQLConnection.stmt = connection.createStatement();
            dropAndCreateTable();
            fillTable();
            System.out.println("Connect to DB");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        SQLConnection.disconnect();
    }
}
