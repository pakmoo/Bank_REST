package com.example.bankcards.exception;

public class BalanceIsLessThanWithdrawAmountException extends RuntimeException {
    public BalanceIsLessThanWithdrawAmountException() {
        System.out.println("Текущий баланс карты меньше суммы вывода.");
    }
}