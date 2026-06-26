package com.arnav.authsystem.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "short_urls")
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // the random short code, e.g. "aB3xK9"  — must be unique
    @Column(unique = true, nullable = false)
    private String shortCode;

    // the original long URL this code points to
    @Column(nullable = false, length = 2048)
    private String longUrl;

    // which user created this link (their username)
    @Column(nullable = false)
    private String owner;

    // how many times this short link has been clicked
    private Long clickCount = 0L;

    // when it was created
    private LocalDateTime createdAt = LocalDateTime.now();
}