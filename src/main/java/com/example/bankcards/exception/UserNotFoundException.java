package com.example.bankcards.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long userId) {
        super("Пользователь с id " + userId + " не найден");
    }

    public UserNotFoundException(String username) {
        super("Пользователь с именем " + username + " не найден");
    }
}