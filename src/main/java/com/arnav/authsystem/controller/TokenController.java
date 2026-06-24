package com.arnav.authsystem.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arnav.authsystem.entities.RefreshToken;
import com.arnav.authsystem.request.AuthRequestDTO;
import com.arnav.authsystem.request.RefreshTokenRequestDTO;
import com.arnav.authsystem.response.JwtResponseDTO;
import com.arnav.authsystem.service.JwtService;
import com.arnav.authsystem.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/v1")
public class TokenController {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private static final Logger log = LoggerFactory.getLogger(TokenController.class);

   @PostMapping("/login")
public ResponseEntity<?> authenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO) {
    try {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequestDTO.getUsername(),
                        authRequestDTO.getPassword()
                )
        );
        if (authentication.isAuthenticated()) {
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
            return ResponseEntity.ok(
                    JwtResponseDTO.builder()
                            .accessToken(jwtService.generateToken(authRequestDTO.getUsername()))
                            .token(refreshToken.getToken())
                            .build()
            );
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
    } catch (Exception e) {
        log.error("Login failed: {}", e.getMessage()); // ADD LOGGER TO THIS CLASS
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login error: " + e.getMessage());
    }
}
    @PostMapping("/refreshToken")
    public JwtResponseDTO refreshToken(@RequestBody RefreshTokenRequestDTO request) {

        return refreshTokenService.findByToken(request.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {

                    String accessToken =
                            jwtService.generateToken(userInfo.getUsername());

                    return JwtResponseDTO.builder()
                            .accessToken(accessToken)
                            .token(request.getToken())
                            .build();

                }).orElseThrow(() ->
                        new RuntimeException("Refresh token not found"));
    }
}