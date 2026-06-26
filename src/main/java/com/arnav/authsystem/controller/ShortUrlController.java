package com.arnav.authsystem.controller;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.arnav.authsystem.entities.ShortUrl;
import com.arnav.authsystem.service.ShortUrlService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    // ---- CREATE a short link (PROTECTED — needs JWT) ----
    // POST /api/shorten body: { "longUrl": "https://..." }
    @PostMapping("/api/shorten")
    public ResponseEntity<ShortUrl> shorten(@RequestBody Map<String, String> body,
            Authentication authentication) {
        String longUrl = body.get("longUrl");
        String owner = authentication.getName(); // username from the JWT (set by JwtAuthFilter)
        ShortUrl created = shortUrlService.createShortUrl(longUrl, owner);
        return ResponseEntity.ok(created);
    }

    // ---- LIST my links (PROTECTED — for the dashboard) ----
    // GET /api/my-links
    @GetMapping("/api/my-links")
    public ResponseEntity<List<ShortUrl>> myLinks(Authentication authentication) {
        String owner = authentication.getName();
        return ResponseEntity.ok(shortUrlService.getUserLinks(owner));
    }

    // ---- REDIRECT (PUBLIC — anyone clicking a short link) ----
// GET /r/{shortCode}  -> 302 redirect to the long URL
@GetMapping("/r/{shortCode}")
public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
    String longUrl = shortUrlService.resolveAndCount(shortCode);
    return ResponseEntity.status(302)
            .location(URI.create(longUrl))   // tells the browser "go here instead"
            .build();
}
    }
