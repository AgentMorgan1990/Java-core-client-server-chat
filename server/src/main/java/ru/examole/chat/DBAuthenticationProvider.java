package ru.examole.chat;

import java.sql.ResultSet;
import java.sql.SQLException;


public class DBAuthenticationProvider implements AuthenticationProvider {

    private DBConnection dbConnection;

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        String nickname = null;
        try {
            ResultSet rs = dbConnection.getStmt().executeQuery(String.format("SELECT nickname FROM users WHERE login = '%s' AND password = '%s';", login, password));
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
            dbConnection.getStmt().execute(String.format("UPDATE users SET nickname = '%s' WHERE nickname = '%s';", newNickname, oldNickname));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return newNickname;
    }


    //todo не добавлять эти методы?
    private void dropAndCreateTable() throws SQLException {
        dbConnection.getStmt().executeUpdate("drop table if exists users;");
        dbConnection.getStmt().executeUpdate(" CREATE TABLE if not exists users (id INTEGER PRIMARY KEY AUTOINCREMENT, nickname  TEXT, login TEXT, password TEXT);");
    }
    //todo не добавлять эти методы?
    private void fillTable() throws SQLException {
        dbConnection.getStmt().executeUpdate("insert into users (login, nickname, password) values ('Bob','SuperBob','100'),('Alan','Wizard','100'),('Jack','DrankMan','100');");
    }

    @Override
    public void init() {
        dbConnection = new DBConnection();
        try {
            dropAndCreateTable();
            fillTable();
            System.out.println("Connect to DB");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isNickBusy(String nickname) {
        try {
            ResultSet rs = dbConnection.getStmt().executeQuery(String.format("SELECT nickname FROM users WHERE nickname = '%s';", nickname));
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void shutdown() {
        dbConnection.disconnect();
    }
}
