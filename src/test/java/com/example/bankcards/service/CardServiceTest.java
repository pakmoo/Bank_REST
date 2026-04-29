package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapping.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionUtil;
import com.example.bankcards.util.MaskingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;
    private CardRequest cardRequest;
    private CardDTO cardDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setRole(Role.USER);

        testCard = Card.builder()
                .id(1L)
                .cardNumber("1234567812345678")
                .encryptedNumber("encrypted123")
                .balance(BigDecimal.valueOf(1000))
                .expirationDate(LocalDate.of(2028, 12, 31))
                .cardStatus(CardStatus.ACTIVE)
                .owner(testUser)
                .build();

        cardRequest = new CardRequest(
                1L,
                "1234567812345678",
                LocalDate.of(2028, 12, 31),
                BigDecimal.valueOf(1000),
                CardStatus.ACTIVE
        );

        cardDTO = new CardDTO(
                MaskingUtil.maskCardNumber("1234567812345678"),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(1000),
                LocalDate.of(2028, 12, 31),
                null
        );
    }

    @Test
    void saveCard_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(encryptionUtil.encrypt(anyString())).thenReturn("encrypted123");
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.cardToCardDTO(any(Card.class))).thenReturn(cardDTO);

        CardDTO result = cardService.saveCard(cardRequest);

        assertThat(result).isNotNull();
        assertThat(result.balance()).isEqualTo(BigDecimal.valueOf(1000));
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void saveCard_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.saveCard(cardRequest))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void findById_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(encryptionUtil.decrypt(anyString())).thenReturn("1234567812345678");
        when(cardMapper.cardToCardDTO(any(Card.class))).thenReturn(cardDTO);

        CardDTO result = cardService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.balance()).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    void findById_CardNotFound_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.findById(1L))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void findAllUserCardsWithPagination_Success() {
        Page<Card> cardPage = new PageImpl<>(List.of(testCard));
        when(cardRepository.findAllByOwnerId(eq(1L), any(PageRequest.class))).thenReturn(cardPage);
        when(cardMapper.cardToCardDTO(any(Card.class))).thenReturn(cardDTO);

        Page<CardDTO> result = cardService.findAllUserCardsWithPagination(1L, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).balance()).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    void blockCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.cardToCardDTO(any(Card.class))).thenReturn(cardDTO);

        CardDTO result = cardService.blockCard(1L);

        assertThat(result).isNotNull();
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void requestBlockCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.cardToCardDTO(any(Card.class))).thenReturn(cardDTO);

        CardDTO result = cardService.requestBlockCard(1L, 1L);

        assertThat(result).isNotNull();
    }

    @Test
    void requestBlockCard_AccessDenied_ThrowsException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        assertThatThrownBy(() -> cardService.requestBlockCard(1L, 999L))
                .isInstanceOf(AccessDeniedException.class);
    }
}