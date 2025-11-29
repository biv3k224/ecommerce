package com.storeinventory.viewer.service.impl;

import com.storeinventory.viewer.entity.User;
import com.storeinventory.viewer.repository.UserRepository;
import com.storeinventory.viewer.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional

    public User createUser(User user) {
        log.info("Creating new user: {}", user.getUsername());

        // Validate unique constraints
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username '" + user.getUsername() + "' is already taken");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email '" + user.getEmail() + "' is already registered");
        }

        // DEBUG: Log password before encoding
        log.info("DEBUG - Original password: '{}'", user.getPassword());

        // Encode password
        String rawPassword = user.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // DEBUG: Log encoded password
        log.info("DEBUG - Encoded password: '{}'", encodedPassword);
        log.info("DEBUG - Password length: {}", encodedPassword.length());

        user.setPassword(encodedPassword);

        // Set default role if not provided
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ADMIN");
        }

        User savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User updateUser(Long id, User userDetails) {
        log.info("Updating user with id: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Check if username is being changed and if new username already exists
        if (!existingUser.getUsername().equals(userDetails.getUsername()) &&
                userRepository.existsByUsername(userDetails.getUsername())) {
            throw new IllegalArgumentException("Username '" + userDetails.getUsername() + "' is already taken");
        }

        // Check if email is being changed and if new email already exists
        if (!existingUser.getEmail().equals(userDetails.getEmail()) &&
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException("Email '" + userDetails.getEmail() + "' is already registered");
        }

        // Update fields
        existingUser.setUsername(userDetails.getUsername());
        existingUser.setEmail(userDetails.getEmail());

        // Only update password if provided (and not empty)
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        if (userDetails.getRole() != null && !userDetails.getRole().isEmpty()) {
            existingUser.setRole(userDetails.getRole());
        }

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }

        userRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExistsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateUserCredentials(String username, String password) {
        log.info("=== DEBUG LOGIN ===");
        log.info("Username attempt: '{}'", username);
        log.info("Password attempt: '{}'", password);

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            log.info("Stored encoded password: '{}'", user.getPassword());
            log.info("Stored password length: {}", user.getPassword().length());

            boolean matches = passwordEncoder.matches(password, user.getPassword());
            log.info("Password matches result: {}", matches);

            return matches;
        }

        log.info("User not found in database");
        return false;
    }
}