package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CardRequest(
        @NotNull(message = "ID пользователя обязателен")
        Long userId,

        @NotBlank(message = "Номер карты обязателен")
        @Pattern(regexp = "\\d{16}", message = "Номер карты должен содержать 16 цифр")
        String cardNumber,

        @NotNull(message = "Срок действия обязателен")
        @Future(message = "Срок действия должен быть в будущем")
        LocalDate expirationDate,

        @DecimalMin(value = "0.0", inclusive = true, message = "Баланс не может быть отрицательным")
        BigDecimal balance,

        @NotNull(message = "Статус карты обязателен")
        CardStatus cardStatus
) {}