package com.arnav.authsystem.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.arnav.authsystem.entities.RefreshToken;
import com.arnav.authsystem.entities.UserInfo;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Integer>
{
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUserInfo(UserInfo userInfo);
}