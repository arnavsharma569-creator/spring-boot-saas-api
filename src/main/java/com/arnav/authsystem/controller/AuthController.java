package com.arnav.authsystem.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arnav.authsystem.dto.UserInfoDto;
import com.arnav.authsystem.entities.RefreshToken;
import com.arnav.authsystem.response.JwtResponseDTO;
import com.arnav.authsystem.service.JwtService;
import com.arnav.authsystem.service.RefreshTokenService;
import com.arnav.authsystem.service.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/v1")
public class AuthController {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserInfoDto userInfoDto) {
        try {

            Boolean isSignedUp = userDetailsService.signupUser(userInfoDto);

            if (Boolean.FALSE.equals(isSignedUp)) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("User already exists");
            }

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfoDto.getUsername());

            String jwtToken = jwtService.generateToken(userInfoDto.getUsername());

            return ResponseEntity.ok(
                    JwtResponseDTO.builder()
                            .accessToken(jwtToken)
                            .token(refreshToken.getToken())
                            .build());

        } catch (Exception ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Exception in User Service");
        }
    }

    
    }
      