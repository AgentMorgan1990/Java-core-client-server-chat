package ru.example.chat;

@FunctionalInterface
public interface Callback {
    void callback(Object... args);
}
