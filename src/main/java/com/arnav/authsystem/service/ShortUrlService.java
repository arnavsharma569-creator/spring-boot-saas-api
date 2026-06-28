package com.arnav.authsystem.service;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.stereotype.Service;

import com.arnav.authsystem.entities.ShortUrl;
import com.arnav.authsystem.entities.UserInfo;
import com.arnav.authsystem.repository.ShortUrlRepository;
import com.arnav.authsystem.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private final UserRepository userRepository;   // NEW: to look up the user's tier

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int FREE_LIMIT = 5;        // free users get 5 links
    private final SecureRandom random = new SecureRandom();

    // ---- CREATE a short link (now with the tier gate) ----
    public ShortUrl createShortUrl(String longUrl, String owner) {

        // 1. fetch the user to read their tier (the STORED state)
        UserInfo user = userRepository.findByUsername(owner)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. if FREE, check the live count against the limit
        if ("FREE".equals(user.getTier())) {
            long currentCount = shortUrlRepository.countByOwner(owner);   // computed live
            if (currentCount >= FREE_LIMIT) {
                // signal: limit hit — controller turns this into an upgrade prompt
                throw new RuntimeException("FREE_LIMIT_REACHED");
            }
        }
        // PAID users skip the check entirely = unlimited

        // 3. proceed to create
        String code = generateUniqueCode();
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(code);
        shortUrl.setLongUrl(longUrl);
        shortUrl.setOwner(owner);
        shortUrl.setClickCount(0L);
        return shortUrlRepository.save(shortUrl);
    }

    public List<ShortUrl> getUserLinks(String owner) {
        return shortUrlRepository.findByOwner(owner);
    }

    public String resolveAndCount(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short link not found"));
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);
        return shortUrl.getLongUrl();
    }

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
            }
            code = sb.toString();
        } while (shortUrlRepository.findByShortCode(code).isPresent());
        return code;
    }
}