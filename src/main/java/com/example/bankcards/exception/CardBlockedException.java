package com.example.bankcards.exception;

public class CardBlockedException extends RuntimeException {

    public CardBlockedException(Long cardId) {
        super("Карта с id " + cardId + " заблокирована. Операция недоступна");
    }
}