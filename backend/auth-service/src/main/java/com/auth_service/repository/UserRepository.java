package com.auth_service.repository;

import com.auth_service.entity.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
