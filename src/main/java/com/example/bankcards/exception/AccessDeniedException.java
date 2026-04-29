package com.example.bankcards.exception;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException() {
        super("У вас нет прав для выполнения этой операции");
    }

    public AccessDeniedException(String message) {
        super(message);
    }
}