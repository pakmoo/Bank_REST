package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "cards")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank(message = "Номер карты не может быть пустым.")
    @Size(min = 16, max = 16, message = "Номер карты должен содержать 16 символов")
    @Column(unique = true, nullable = false)
    private String cardNumber;

    @Column(name = "encrypted_number", nullable = false, unique = true)
    @JsonIgnore
    private String encryptedNumber;  // Зашифрованный номер в БД

    @Transient
    private String maskedNumber;  // Маскированный номер для API (**** **** **** 1234)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private User owner;

    @NotNull(message = "Срок действия обязателен")
    @Future(message = "Срок действия карты должен быть в будущем")
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @NotNull(message = "Статус карты обязателен")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus cardStatus;

    @NotNull(message = "Баланс обязателен")
    @DecimalMin(value = "0.0", inclusive = true, message = "Баланс не может быть отрицательным")
    @Digits(integer = 15, fraction = 2)
    @Column(columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal balance;

    @OneToMany(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List <Transaction> listTransaction;

    // Метод для блокировки
    public void block() {
        this.cardStatus = CardStatus.BLOCKED;
    }

    // Метод для активации
    public void activate() {
        this.cardStatus = CardStatus.ACTIVE;
    }

}
