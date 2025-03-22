package com.devops26.user.service;

import com.devops26.user.entity.User;

public interface UserService {
    User findByUserId(Integer userId);
    Boolean register(User user);
    String login(String phone, String password);
    Boolean updateUser(User user);
    Boolean verifyPwd(String password);
} 