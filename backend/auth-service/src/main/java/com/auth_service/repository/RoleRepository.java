package com.auth_service.repository;

import com.auth_service.entity.Role;
import com.auth_service.entity.RoleType;

import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(RoleType name);
}
