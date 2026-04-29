package com.example.bankcards.util;

import java.time.LocalDate;

public class DateUtil {

    // Проверка, истек ли срок
    public static boolean isExpired(LocalDate expirationDate) {
        return LocalDate.now().isAfter(expirationDate);
    }

    // Проверка, активен ли срок (не истек)
    public static boolean isValid(LocalDate expirationDate) {
        return !isExpired(expirationDate);
    }

    // Сколько дней осталось
    public static long daysUntilExpiration(LocalDate expirationDate) {
        return LocalDate.now().until(expirationDate).getDays();
    }
}