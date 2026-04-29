package com.example.bankcards.dto;

import java.util.List;

public record UserDTO (String username,
                       String role,
                       List<CardDTO> cards)
{}
