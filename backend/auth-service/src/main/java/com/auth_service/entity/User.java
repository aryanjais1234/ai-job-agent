package com.auth_service.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String firstName;
    String lastName;
    String email;
    String password;
    Boolean enabled;
    Boolean emailVerified;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
