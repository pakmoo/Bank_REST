package com.example.bankcards.dto;
//ответ после входа (токен)
public record LoginResponse(
        String token,
        String username,
        String role
) {}