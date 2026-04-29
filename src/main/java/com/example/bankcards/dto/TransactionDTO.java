package com.example.bankcards.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDTO(
        String transactionName,
        LocalDateTime timeTransfer,
        BigDecimal amount,
        String descriptionTransaction,
        String operationType
) {}