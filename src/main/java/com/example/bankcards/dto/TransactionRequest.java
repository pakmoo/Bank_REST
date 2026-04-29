package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "Назначение перевода обязательно")
        String purpose,

        @NotNull(message = "Сумма обязательна")
        @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
        BigDecimal amount,

        @NotNull(message = "ID карты отправителя обязателен")
        Long fromCardId,

        @NotNull(message = "ID карты получателя обязателен")
        Long toCardId
) {}