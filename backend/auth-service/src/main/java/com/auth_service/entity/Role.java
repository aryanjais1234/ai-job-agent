package com.auth_service.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    RoleType name;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
