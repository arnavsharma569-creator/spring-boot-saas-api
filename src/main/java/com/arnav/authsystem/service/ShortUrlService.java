package com.arnav.authsystem.service;

import com.arnav.authsystem.entities.ShortUrl;
import com.arnav.authsystem.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;

    // characters used to build a short code
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    // ---- CREATE a short link for a given user ----
    public ShortUrl createShortUrl(String longUrl, String owner) {
        String code = generateUniqueCode();      // make a code not already in use

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(code);
        shortUrl.setLongUrl(longUrl);
        shortUrl.setOwner(owner);
        shortUrl.setClickCount(0L);

        return shortUrlRepository.save(shortUrl);  // save the mapping
    }

    // ---- LIST all links owned by a user (for the dashboard) ----
    public List<ShortUrl> getUserLinks(String owner) {
        return shortUrlRepository.findByOwner(owner);
    }

    // ---- REDIRECT lookup: find the long URL + count the click ----
    public String resolveAndCount(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("Short link not found"));

        shortUrl.setClickCount(shortUrl.getClickCount() + 1);   // increment clicks
        shortUrlRepository.save(shortUrl);                       // persist the new count

        return shortUrl.getLongUrl();                            // give back the destination
    }

    // ---- helper: generate a random code, retry if it already exists ----
    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
            }
            code = sb.toString();
        } while (shortUrlRepository.findByShortCode(code).isPresent());  // avoid collisions
        return code;
    }
}