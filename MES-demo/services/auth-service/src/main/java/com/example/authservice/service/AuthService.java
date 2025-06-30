package com.example.authservice.service;

import com.example.authservice.entity.User;

public interface AuthService {
    String login(String username, String password);
    void register(User user);
}

