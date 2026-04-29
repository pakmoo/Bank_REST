package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.BalanceIsLessThanWithdrawAmountException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapping.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.EncryptionUtil;
import com.example.bankcards.util.MaskingUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CardService {
    TransactionRepository transactionRepository;
    CardRepository cardRepository;
    UserRepository userRepository;
    CardMapper cardMapper;
    EncryptionUtil encryptionUtil;

    public CardService(CardRepository cardRepository, UserRepository userRepository, CardMapper cardMapper, TransactionRepository transactionRepository, EncryptionUtil encryptionUtil) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cardMapper = cardMapper;
        this.transactionRepository = transactionRepository;
        this.encryptionUtil = encryptionUtil;
    }

    @Transactional
    //создание новой карты для пользователя
    public CardDTO saveCard(CardRequest cardRequest){
        User owner = userRepository.findById(cardRequest.userId())
                .orElseThrow(() -> new UserNotFoundException(cardRequest.userId()));
        String encryptedNumber = encryptionUtil.encrypt(cardRequest.cardNumber());
        Card card = new Card();
        card.setCardNumber(cardRequest.cardNumber());
        card.setEncryptedNumber(encryptedNumber);
        card.setBalance(cardRequest.balance());
        card.setExpirationDate(cardRequest.expirationDate());
        card.setCardStatus(cardRequest.cardStatus());
        card.setOwner(owner);

        Card savedCard = cardRepository.save(card);
        return cardMapper.cardToCardDTO(savedCard);
    }

    public CardDTO findById(Long card_id){
        Card card = cardRepository.findById(card_id)
                .orElseThrow(() -> new CardNotFoundException(card_id));
        //Дешифруем номер
        String decryptedNumber = encryptionUtil.decrypt(card.getEncryptedNumber());
        // Маскируем
        String masked = MaskingUtil.maskCardNumber(decryptedNumber);
        card.setMaskedNumber(masked);

        return cardMapper.cardToCardDTO(card);
    }

    public List<CardDTO> findAll(){
        return cardRepository.findAll().stream().map(cardMapper::cardToCardDTO).collect(Collectors.toList());
    }
    //поиск всех карт конкретного пользователя
    public List<CardDTO> findAllUserCard(Long user_id){
        return cardRepository.findAllByOwnerId(user_id).stream().map(cardMapper::cardToCardDTO).collect(Collectors.toList());
    }

    public Page<CardDTO> findAllUserCardsWithPagination(Long userId, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        return cardRepository.findAllByOwnerId(userId, pageable)
                .map(cardMapper::cardToCardDTO);
    }

    @Transactional
    public void deleteCard(Long card_id){
        if (!cardRepository.existsById(card_id)){
            throw new RuntimeException("Не удалось удалить: карта с id" + card_id + "не найдена");
        }
        cardRepository.deleteById(card_id);
    }

    @Transactional
    public CardDTO depositToCard(Long card_id, BigDecimal amount){
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new RuntimeException("Карта не найдена."));
        card.setBalance(card.getBalance().add(amount));
        Transaction depositTrans = new Transaction();
        depositTrans.setName("Пополнение счёта");
        depositTrans.setAmount(amount);
        depositTrans.setTime(LocalDateTime.now());
        depositTrans.setCard(card);
        transactionRepository.save(depositTrans);
        return cardMapper.cardToCardDTO(cardRepository.save(card));
    }

    @Transactional
    public CardDTO withdrawFromCard(Long card_id, BigDecimal amount){
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new CardNotFoundException(card_id));
        if (card.getBalance().compareTo(amount) >= 0){
            card.setBalance((card.getBalance().subtract(amount)));
            Transaction withdrawTrans = new Transaction();
            withdrawTrans.setName("Снятие наличных");
            withdrawTrans.setAmount(amount);
            withdrawTrans.setTime(LocalDateTime.now());
            withdrawTrans.setCard(card);
            transactionRepository.save(withdrawTrans);
            return cardMapper.cardToCardDTO(cardRepository.save(card));
        }
        else {throw new BalanceIsLessThanWithdrawAmountException();}
    }

    // Блокировка карты (только для ADMIN)
    @Transactional
    public CardDTO blockCard(Long card_id){
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new CardNotFoundException(card_id));
        card.block();
        return cardMapper.cardToCardDTO(cardRepository.save(card));
    }

    // Активация карты (только для ADMIN)
    @Transactional
    public CardDTO activateCard(Long card_id){
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new CardNotFoundException(card_id));
        card.activate();
        return cardMapper.cardToCardDTO(cardRepository.save(card));
    }

    // Запрос на блокировку (для USER - блокирует только свои карты)
    @Transactional
    public CardDTO requestBlockCard(Long card_id, Long user_id){
        Card card = cardRepository.findById(card_id).orElseThrow(() -> new CardNotFoundException(card_id));

        if (card.getOwner().getId() != user_id) {
            throw new AccessDeniedException("Нельзя заблокировать чужую карту");
        }

        card.block();
        return cardMapper.cardToCardDTO(cardRepository.save(card));
    }
}