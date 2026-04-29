package com.example.bankcards.service;

import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.dto.UserDTO;
import com.example.bankcards.dto.UserRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.mapping.UserMapper;
import com.example.bankcards.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;  // для хеширования пароля

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // Создание пользователя (регистрация)
    @Transactional
    public UserDTO createUser(UserRequest userRequest){
        // Проверка уникальности username
        if (userRepository.existsByUsername(userRequest.username())) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }

        User user = userMapper.dtoToUser(userRequest);

        // Хеширование пароля
        user.setPassword(passwordEncoder.encode(userRequest.password()));

        // Если роль не указана, ставим USER по умолчанию
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        User savedUser = userRepository.save(user);
        return userMapper.userToDTO(savedUser);
    }

    // Поиск пользователя по ID
    public UserDTO getUserById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userMapper.userToDTO(user);
    }

    // Поиск пользователя по username (для аутентификации)
    public User findByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    // Удаление пользователя (только для ADMIN)
    @Transactional
    public void deleteUser(Long userId){
        if (!userRepository.existsById(userId)){
            throw new RuntimeException("Пользователь не найден");
        }
        userRepository.deleteById(userId);
    }

    // Список всех пользователей (только для ADMIN)
    public List<UserDTO> findAllUsers(){
        return userRepository.findAll().stream()
                .map(userMapper::userToDTO)
                .collect(Collectors.toList());
    }
}