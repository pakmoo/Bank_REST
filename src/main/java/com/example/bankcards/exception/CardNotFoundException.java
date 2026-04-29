package com.example.bankcards.exception;

public class CardNotFoundException extends RuntimeException {

    public CardNotFoundException(Long cardId) {
        super("Карта с id " + cardId + " не найдена");
    }

    public CardNotFoundException(String message) {
        super(message);
    }
}