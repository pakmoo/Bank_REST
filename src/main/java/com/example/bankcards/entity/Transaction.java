package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.TransactionOperationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Column(nullable = false)
    private LocalDateTime time;

    @NotNull
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionOperationType operationType;  // WITHDRAWAL или DEPOSIT

    // ссылка на связанную транзакцию (для парных операций)
    //@OneToOne
    //@JoinColumn(name = "paired_transaction_id")
    //private Transaction pairedTransaction;

    @PrePersist
    protected void onCreate() {
        if (time == null) {
            time = LocalDateTime.now();
        }
    }
}
