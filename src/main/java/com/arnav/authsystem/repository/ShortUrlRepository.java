package com.arnav.authsystem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arnav.authsystem.entities.ShortUrl;

public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    // look up a link by its short code (for the redirect)
    Optional<ShortUrl> findByShortCode(String shortCode);

    // list all links created by one user (for the dashboard)
    List<ShortUrl> findByOwner(String owner);

    // count how many links a user has made (for the free-tier limit later)
    long countByOwner(String owner);
}