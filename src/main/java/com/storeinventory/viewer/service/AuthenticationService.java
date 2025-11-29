package com.storeinventory.viewer.service;

import com.storeinventory.viewer.dto.AuthenticationRequest;
import com.storeinventory.viewer.dto.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
    boolean validateToken(String token);
    String getUsernameFromToken(String token);
    String generateToken(String username); // Add this method declaration
}