package com.auth_service.controller.api.v1.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/auth")
public class AuthController {
    @PostMapping("/register")
    void registerUser(){

    }

    @PostMapping("/login")
    void login(){

    }

    @PostMapping("/refresh")
    void refresh(){
        
    }
}
