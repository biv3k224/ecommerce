package com.storeinventory.viewer.service;

import com.storeinventory.viewer.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUser(User user);
    Optional<User> getUserById(Long id);
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByEmail(String email);
    List<User> getAllUsers();
    User updateUser(Long id, User userDetails);
    void deleteUser(Long id);
    boolean userExistsByUsername(String username);
    boolean userExistsByEmail(String email);
    boolean validateUserCredentials(String username, String password);
}
