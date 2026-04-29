package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@ExtendWith(MockitoExtension.class)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    private CardDTO cardDTO;
    private CardRequest cardRequest;

    @BeforeEach
    void setUp() {
        cardDTO = new CardDTO(
                "**** **** **** 5678",
                CardStatus.ACTIVE,
                BigDecimal.valueOf(1000),
                LocalDate.of(2028, 12, 31),
                null
        );

        cardRequest = new CardRequest(
                1L,
                "1234567812345678",
                LocalDate.of(2028, 12, 31),
                BigDecimal.valueOf(1000),
                CardStatus.ACTIVE
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveCard_ShouldReturnCardDTO() throws Exception {
        when(cardService.saveCard(any(CardRequest.class))).thenReturn(cardDTO);

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 5678"))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    @WithMockUser
    void findById_ShouldReturnCardDTO() throws Exception {
        when(cardService.findById(1L)).thenReturn(cardDTO);

        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maskedNumber").value("**** **** **** 5678"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAll_ShouldReturnListOfCards() throws Exception {
        when(cardService.findAll()).thenReturn(List.of(cardDTO));

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].maskedNumber").value("**** **** **** 5678"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/cards/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void depositToCard_ShouldReturnCardDTO() throws Exception {
        when(cardService.depositToCard(eq(1L), any(BigDecimal.class))).thenReturn(cardDTO);

        mockMvc.perform(post("/api/cards/1/deposit")
                        .param("amount", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_ShouldReturnCardDTO() throws Exception {
        when(cardService.blockCard(1L)).thenReturn(cardDTO);

        mockMvc.perform(post("/api/cards/1/block"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_ShouldReturnCardDTO() throws Exception {
        when(cardService.activateCard(1L)).thenReturn(cardDTO);

        mockMvc.perform(post("/api/cards/1/activate"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void requestBlockCard_ShouldReturnCardDTO() throws Exception {
        when(cardService.requestBlockCard(eq(1L), eq(1L))).thenReturn(cardDTO);

        mockMvc.perform(post("/api/cards/1/request-block")
                        .param("userId", "1"))
                .andExpect(status().isOk());
    }
}