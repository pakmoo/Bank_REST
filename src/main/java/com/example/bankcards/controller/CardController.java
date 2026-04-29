package com.example.bankcards.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    // Создание карты (ADMIN)
    @PostMapping
    public CardDTO saveCard(@Valid @RequestBody CardRequest card){
        return cardService.saveCard(card);
    }

    // Просмотр карты по ID
    @GetMapping("/{id}")
    public CardDTO findById(@PathVariable Long id){
        return cardService.findById(id);
    }

    // Удаление карты (ADMIN)
    @DeleteMapping("/{id}")
    public void deleteCard(@PathVariable Long id){
        cardService.deleteCard(id);
    }

    // Все карты (ADMIN)
    @GetMapping
    public List<CardDTO> findAll(){
        return cardService.findAll();
    }

    // Все карты пользователя (USER)
    @GetMapping("/user/{userId}")
    public Page<CardDTO> findAllUserCards(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return cardService.findAllUserCardsWithPagination(userId, page, size);
    }

    // Пополнение карты
    @PostMapping("/{id}/deposit")
    public CardDTO depositToCard(@PathVariable Long id, @RequestParam BigDecimal amount){
        return cardService.depositToCard(id, amount);
    }

    // Списание с карты
    @PostMapping("/{id}/withdraw")
    public CardDTO withdrawFromCard(@PathVariable Long id, @RequestParam BigDecimal amount){
        return cardService.withdrawFromCard(id, amount);
    }

    // Блокировка карты (ADMIN)
    @PostMapping("/{id}/block")
    public CardDTO blockCard(@PathVariable Long id){
        return cardService.blockCard(id);
    }

    // Активация карты (ADMIN)
    @PostMapping("/{id}/activate")
    public CardDTO activateCard(@PathVariable Long id){
        return cardService.activateCard(id);
    }

    // Запрос на блокировку (USER)
    @PostMapping("/{id}/request-block")
    public CardDTO requestBlockCard(@PathVariable Long id, @RequestParam Long userId){
        return cardService.requestBlockCard(id, userId);
    }
}