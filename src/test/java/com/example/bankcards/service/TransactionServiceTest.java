package com.example.bankcards.service;

import com.example.bankcards.config.KafkaSendTemplates;
import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.TransactionOperationType;
import com.example.bankcards.exception.*;
import com.example.bankcards.mapping.TransactionMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserService userService;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private KafkaSendTemplates kafkaSendTemplates;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Card cardFrom;
    private Card cardTo;
    private TransactionRequest transactionRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setRole(Role.USER);

        cardFrom = Card.builder()
                .id(1L)
                .cardNumber("1234567812345678")
                .balance(BigDecimal.valueOf(1000))
                .expirationDate(LocalDate.of(2028, 12, 31))
                .cardStatus(CardStatus.ACTIVE)
                .owner(testUser)
                .build();

        cardTo = Card.builder()
                .id(2L)
                .cardNumber("8765432187654321")
                .balance(BigDecimal.valueOf(500))
                .expirationDate(LocalDate.of(2028, 12, 31))
                .cardStatus(CardStatus.ACTIVE)
                .owner(testUser)
                .build();

        transactionRequest = new TransactionRequest(
                "transfer",
                BigDecimal.valueOf(200),
                1L,
                2L
        );
    }

    @Test
    void transactionToCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(cardTo));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(kafkaSendTemplates).sendMessage(anyString());

        TransactionDTO result = transactionService.transactionToCard(transactionRequest);

        assertThat(result).isNotNull();
        // Проверяем, что балансы изменились
        assertThat(cardFrom.getBalance()).isEqualTo(BigDecimal.valueOf(800));
        assertThat(cardTo.getBalance()).isEqualTo(BigDecimal.valueOf(700));

        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(kafkaSendTemplates, times(2)).sendMessage(anyString());
    }

    @Test
    void transactionToCard_SameCard_ThrowsException() {
        TransactionRequest sameCardRequest = new TransactionRequest(
                "transfer",
                BigDecimal.valueOf(200),
                1L,
                1L
        );

        assertThatThrownBy(() -> transactionService.transactionToCard(sameCardRequest))
                .isInstanceOf(SameCardTransferException.class);
    }

    @Test
    void transactionToCard_CardFromNotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.transactionToCard(transactionRequest))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void transactionToCard_CardsFromDifferentUsers_ThrowsException() {
        User otherUser = new User();
        otherUser.setId(2L);
        cardTo.setOwner(otherUser);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(cardTo));

        assertThatThrownBy(() -> transactionService.transactionToCard(transactionRequest))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void transactionToCard_CardFromBlocked_ThrowsException() {
        cardFrom.setCardStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(cardTo));

        assertThatThrownBy(() -> transactionService.transactionToCard(transactionRequest))
                .isInstanceOf(CardBlockedException.class);
    }

    @Test
    void transactionToCard_InsufficientFunds_ThrowsException() {
        TransactionRequest largeAmountRequest = new TransactionRequest(
                "transfer",
                BigDecimal.valueOf(2000),
                1L,
                2L
        );

        when(cardRepository.findById(1L)).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(cardTo));

        assertThatThrownBy(() -> transactionService.transactionToCard(largeAmountRequest))
                .isInstanceOf(BalanceIsLessThanAmountException.class);
    }
}