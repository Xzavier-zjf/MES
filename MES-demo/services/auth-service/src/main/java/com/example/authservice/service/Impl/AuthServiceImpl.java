package com.example.authservice.service.Impl;

import com.example.authservice.entity.User;
import com.example.authservice.mapper.UserMapper;
import com.example.authservice.service.AuthService;
import com.example.authservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;

    @Override
    public String login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new RuntimeException("用户名或密码错误");
        }
        return JwtUtil.generateToken(username); // 生成 JWT
    }

    @Override
    public void register(User user) {
        User existing = userMapper.selectByUsername(user.getUsername());
        if (existing != null) throw new RuntimeException("用户名已存在");

        userMapper.insert(user);
    }
}
