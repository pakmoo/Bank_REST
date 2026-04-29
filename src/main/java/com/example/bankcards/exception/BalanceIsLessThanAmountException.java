package com.example.bankcards.exception;

public class BalanceIsLessThanAmountException extends RuntimeException{
    public BalanceIsLessThanAmountException(){
        System.out.println("Сумма перевода меньше текущего баланса карты");
    }
}
