package com.arnav.authsystem.repository;

import com.arnav.authsystem.entities.UserInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserInfo, String>
{
    Optional<UserInfo> findByUsername(String username);
}