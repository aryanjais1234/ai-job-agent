package com.auth_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {

		System.out.println("Default TZ: " + TimeZone.getDefault().getID());
		System.out.println("ZoneId: " + ZoneId.systemDefault());
		System.out.println(System.getProperty("user.timezone"));
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
