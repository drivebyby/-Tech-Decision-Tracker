package com.example.fullstack.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.fullstack.dto.UserCreateRequest;
import com.example.fullstack.entity.User;

public interface UserService extends IService<User> {

    IPage<User> pageUsers(long page, long size);

    User createUser(UserCreateRequest request);
}
