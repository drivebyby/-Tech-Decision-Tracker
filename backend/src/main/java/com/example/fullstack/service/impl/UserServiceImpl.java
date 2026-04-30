package com.example.fullstack.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.fullstack.dto.UserCreateRequest;
import com.example.fullstack.entity.User;
import com.example.fullstack.mapper.UserMapper;
import com.example.fullstack.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public IPage<User> pageUsers(long page, long size) {
        return page(new Page<>(page, size), Wrappers.<User>lambdaQuery()
                .select(User::getId, User::getUsername, User::getEmail, User::getStatus, User::getCreatedAt, User::getUpdatedAt)
                .orderByDesc(User::getId));
    }

    @Override
    public User createUser(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword("");
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        save(user);
        user.setPassword(null);
        return user;
    }
}
