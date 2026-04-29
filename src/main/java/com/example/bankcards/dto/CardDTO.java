package com.example.bankcards.dto;
import com.example.bankcards.entity.enums.CardStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CardDTO(
        String maskedNumber,
        CardStatus cardStatus,
        BigDecimal balance,
        LocalDate expirationDate,
        List<TransactionDTO> transactions
) {}
