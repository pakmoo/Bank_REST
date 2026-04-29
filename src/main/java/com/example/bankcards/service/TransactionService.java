package com.example.bankcards.service;


import com.example.bankcards.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.config.KafkaSendTemplates;
import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.mapping.TransactionMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.entity.enums.TransactionOperationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    CardRepository cardRepository;
    TransactionRepository transactionRepository;
    UserService userService;
    TransactionMapper transactionMapper;
    KafkaSendTemplates kafkaSendTemplates;

    public TransactionService(CardRepository cardRepository, TransactionRepository transactionRepository,
                              UserService userService, TransactionMapper transactionMapper, KafkaSendTemplates kafkaSendTemplates) {
        this.cardRepository = cardRepository;
        this.transactionRepository = transactionRepository;
        this.userService = userService;
        this.transactionMapper = transactionMapper;
        this.kafkaSendTemplates = kafkaSendTemplates;
    }

    public TransactionDTO dtoToTransaction(TransactionRequest transactionRequest){
        Transaction transaction = transactionMapper.dtoToTransaction(transactionRequest);
        transaction.setTime(LocalDateTime.now());
        return transactionMapper.transactionToDTO(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionDTO transactionToCard(TransactionRequest transactionRequest){
        // Проверка: нельзя перевести на ту же карту
        if (transactionRequest.fromCardId().equals(transactionRequest.toCardId())) {
            throw new SameCardTransferException();
        }

        // Ищем карты
        Card cardFrom = cardRepository.findById(transactionRequest.fromCardId())
                .orElseThrow(() -> new CardNotFoundException(transactionRequest.fromCardId()));
        Card cardTo = cardRepository.findById(transactionRequest.toCardId())
                .orElseThrow(() -> new CardNotFoundException(transactionRequest.toCardId()));

        // Проверка принадлежности одному пользователю
        if (cardFrom.getOwner().getId() != cardTo.getOwner().getId()) {
            throw new AccessDeniedException("Перевод возможен только между своими картами");
        }

        // Проверка статуса карт
        if (cardFrom.getCardStatus() != CardStatus.ACTIVE) {
            throw new CardBlockedException(transactionRequest.fromCardId());
        }
        if (cardTo.getCardStatus() != CardStatus.ACTIVE) {
            throw new CardBlockedException(transactionRequest.toCardId());
        }

        // Проверка баланса
        if (cardFrom.getBalance().compareTo(transactionRequest.amount()) >= 0){
            cardFrom.setBalance(cardFrom.getBalance().subtract(transactionRequest.amount()));
            cardTo.setBalance(cardTo.getBalance().add(transactionRequest.amount()));

            // Транзакция для отправителя
            Transaction newTransactionFrom = Transaction.builder()
                    .name("перевод с: " + cardFrom.getCardNumber() + " на " + cardTo.getCardNumber())
                    .card(cardFrom)
                    .time(LocalDateTime.now())
                    .amount(transactionRequest.amount())
                    .operationType(TransactionOperationType.WITHDRAWAL)
                    .build();
            transactionRepository.save(newTransactionFrom);

            // Транзакция для получателя
            Transaction newTransactionTo = Transaction.builder()
                    .name("перевод " + transactionRequest.amount())
                    .card(cardTo)
                    .time(LocalDateTime.now())
                    .amount(transactionRequest.amount())
                    .operationType(TransactionOperationType.DEPOSIT)
                    .build();
            transactionRepository.save(newTransactionTo);

            cardRepository.save(cardFrom);
            cardRepository.save(cardTo);

            kafkaSendTemplates.sendMessage("Снятие с: " + cardFrom.getCardNumber());
            kafkaSendTemplates.sendMessage("Пополнение " + transactionRequest.amount());

            //Возвращаем DTO транзакции отправителя (или получателя)
            return transactionMapper.transactionToDTO(newTransactionFrom);
        } else {
            throw new BalanceIsLessThanAmountException();
        }
    }
    // Получить все транзакции по карте
    public List<TransactionDTO> getTransactionsByCardId(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));

        return card.getListTransaction().stream()
                .map(transactionMapper::transactionToDTO)
                .collect(Collectors.toList());
    }
}