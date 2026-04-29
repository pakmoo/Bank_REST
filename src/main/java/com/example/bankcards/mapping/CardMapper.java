package com.example.bankcards.mapping;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.util.MaskingUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(source = "cardNumber", target = "maskedNumber", qualifiedByName = "maskCardNumber")
    @Mapping(source = "listTransaction", target = "transactions")
    CardDTO cardToCardDTO(Card card);

    @Named("maskCardNumber")
    default String maskCardNumber(String cardNumber) {
        return MaskingUtil.maskCardNumber(cardNumber);
    }
}