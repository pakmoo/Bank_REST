package com.example.bankcards.service;

import com.example.bankcards.dto.LoginRequest;
import com.example.bankcards.dto.LoginResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse authenticate(LoginRequest request) {
        // 1. Ищем пользователя
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new RuntimeException("Неверное имя пользователя или пароль"));

        // 2. Проверяем пароль
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new RuntimeException("Неверное имя пользователя или пароль");
        }

        // 3. Генерируем JWT токен
        String token = jwtUtil.generateToken(user.getUsername());

        // 4. Возвращаем ответ
        return new LoginResponse(token, user.getUsername(), user.getRole().name());
    }
}