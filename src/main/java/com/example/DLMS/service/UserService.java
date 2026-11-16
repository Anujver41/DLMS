package com.example.DLMS.service;

import com.example.DLMS.dto.UserRegistrationDto;
import com.example.DLMS.entity.User;
import com.example.DLMS.enums.Role;
import com.example.DLMS.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("An account with email " + dto.getEmail() + " already exists.");
        }

        User user = User.builder()
                .fullName(dto.getFullName().trim())
                .email(dto.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .role(Role.ROLE_USER)
                .active(true)
                .build();

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public List<User> getAllMembers() {
        return userRepository.findByRole(Role.ROLE_USER);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void toggleUserActive(Long userId) {
        User user = getById(userId);
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    public long countMembers() {
        return userRepository.countByRole(Role.ROLE_USER);
    }
}

