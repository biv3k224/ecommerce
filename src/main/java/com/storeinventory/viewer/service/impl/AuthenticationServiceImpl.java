package com.storeinventory.viewer.service.impl;

import com.storeinventory.viewer.dto.AuthenticationRequest;
import com.storeinventory.viewer.dto.AuthenticationResponse;
import com.storeinventory.viewer.service.AuthenticationService;
import com.storeinventory.viewer.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    @Value("${jwt.secret:myDefaultSecretKeyThatIsLongEnoughForHS512Algorithm1234567890}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long jwtExpirationMs;

    public AuthenticationServiceImpl(UserService userService) {
        this.userService = userService;
    }

    private SecretKey getSigningKey() {
        if (jwtSecret.length() < 64) {
            return Keys.hmacShaKeyFor(jwtSecret.getBytes());
        }
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Authenticating user: {}", request.getUsername());

        boolean isValid = userService.validateUserCredentials(request.getUsername(), request.getPassword());

        if (!isValid) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = generateToken(request.getUsername());

        log.info("Authentication successful for user: {}", request.getUsername());
        return new AuthenticationResponse(token, request.getUsername(), "Login successful");
    }

    @Override
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }
}