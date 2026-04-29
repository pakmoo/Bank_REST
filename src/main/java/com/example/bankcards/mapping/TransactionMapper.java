package com.example.bankcards.mapping;

import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    // Входящие данные
    @Mapping(source = "purpose", target = "name")
    Transaction dtoToTransaction(TransactionRequest transactionRequest);

    // Исходящие данные
    @Mapping(source = "name", target = "transactionName")
    @Mapping(source = "time", target = "timeTransfer")
    TransactionDTO transactionToDTO(Transaction transaction);
}