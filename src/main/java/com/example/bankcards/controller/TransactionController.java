package com.example.bankcards.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.service.TransactionService;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Перевод между своими картами
    @PostMapping("/transfer")
    public TransactionDTO transfer(@Valid @RequestBody TransactionRequest transactionRequest) {
        return transactionService.transactionToCard(transactionRequest);
    }

    // История транзакций по карте (USER видит свои)
    @GetMapping("/card/{cardId}")
    public List<TransactionDTO> getTransactionsByCard(@PathVariable Long cardId) {
        return transactionService.getTransactionsByCardId(cardId);
    }
}