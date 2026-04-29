package com.example.bankcards.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.bankcards.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {

    // Для пагинации
    Page<Card> findAllByOwnerId(Long ownerId, Pageable pageable);

    // Для списка без пагинации (добавь этот метод)
    List<Card> findAllByOwnerId(Long ownerId);

    // Для проверки принадлежности
    boolean existsByIdAndOwnerId(Long cardId, Long ownerId);
}