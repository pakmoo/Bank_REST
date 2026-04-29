package com.example.bankcards.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.dto.UserRequest;
import com.example.bankcards.service.UserService;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Регистрация нового пользователя
    @PostMapping("/register")
    public UserDTO registerUser(@Valid @RequestBody UserRequest userRequest) {
        return userService.createUser(userRequest);
    }

    // Получить пользователя по ID (только ADMIN)
    @GetMapping("/{id}")
    public UserDTO findById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // Удалить пользователя по ID (только ADMIN)
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // Получить всех пользователей (только ADMIN)
    @GetMapping
    public List<UserDTO> findAll() {
        return userService.findAllUsers();
    }
}