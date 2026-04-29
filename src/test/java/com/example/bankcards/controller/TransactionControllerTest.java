package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionDTO;
import com.example.bankcards.dto.TransactionRequest;
import com.example.bankcards.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    private TransactionDTO transactionDTO;
    private TransactionRequest transactionRequest;

    @BeforeEach
    void setUp() {
        transactionDTO = new TransactionDTO(
                "перевод между картами",
                LocalDateTime.now(),
                BigDecimal.valueOf(200),
                null,
                "TRANSFER"
        );

        transactionRequest = new TransactionRequest(
                "transfer",
                BigDecimal.valueOf(200),
                1L,
                2L
        );
    }

    @Test
    @WithMockUser
    void transfer_ShouldReturnTransactionDTO() throws Exception {
        when(transactionService.transactionToCard(any(TransactionRequest.class)))
                .thenReturn(transactionDTO);

        mockMvc.perform(post("/api/transactions/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(200));
    }

    @Test
    @WithMockUser
    void getTransactionsByCard_ShouldReturnList() throws Exception {
        when(transactionService.getTransactionsByCardId(1L))
                .thenReturn(List.of(transactionDTO));

        mockMvc.perform(get("/api/transactions/card/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(200));
    }
}