package com.example.bankcards.exception;

public class SameCardTransferException extends RuntimeException {

    public SameCardTransferException() {
        super("Нельзя перевести деньги на ту же самую карту");
    }
}