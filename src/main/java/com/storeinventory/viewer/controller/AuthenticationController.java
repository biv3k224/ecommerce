package com.storeinventory.viewer.controller;

import com.storeinventory.viewer.dto.AuthenticationRequest;
import com.storeinventory.viewer.dto.AuthenticationResponse;
import com.storeinventory.viewer.entity.User;
import com.storeinventory.viewer.service.AuthenticationService;
import com.storeinventory.viewer.service.UserService;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationService authenticationService;
    private final UserService userService;

    // ✅ Constructor manually added (replaces Lombok @RequiredArgsConstructor)
    public AuthenticationController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationRequest request) {
        log.info("Authentication attempt for user: {}", request.getUsername());

        try {
            AuthenticationResponse response = authenticationService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthenticationResponse(null, null, "Invalid credentials"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        log.info("Registration attempt for user: {}", user.getUsername());

        try {
            User createdUser = userService.createUser(user);

            // Return success response without auto-login
            AuthenticationResponse response = new AuthenticationResponse(
                    null,
                    createdUser.getUsername(),
                    "Registration successful. Please login with your credentials."
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during registration");
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam String token) {
        log.info("Token validation request");

        boolean isValid = authenticationService.validateToken(token);
        if (isValid) {
            return ResponseEntity.ok().body("Token is valid");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }
}
