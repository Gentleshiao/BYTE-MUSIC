package com.devops26.user.service.impl;

import com.devops26.user.entity.Songlist;
import com.devops26.user.exception.TuneIslandException;
import com.devops26.user.feign.MusicFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import com.devops26.user.entity.User;
import com.devops26.user.repository.UserRepository;
import com.devops26.user.service.UserService;
import com.devops26.user.util.TokenUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenUtil tokenUtil;

    @Autowired
    MusicFeign musicFeign;

    @Override
    public User findByUserId(Integer userId) {
        return userRepository.findByUserId(userId);
    }

    @Override
    public Boolean register(User user) {
        try {
            if (userRepository.findByPhone(user.getPhone()) != null) {
                throw TuneIslandException.phoneAlreadyExists();
            }
            if (user.getImageUrl() == null || user.getImageUrl().isEmpty()) {
                user.setImageUrl("https://disney428-a.obs.cn-north-4.myhuaweicloud.com:443/%E9%BB%98%E8%AE%A4%E5%A4%B4%E5%83%8F.jpg");
            }
            user.setPassword(DigestUtils.md5DigestAsHex((user.getPassword() + "TuneIsland").getBytes()));
            user = userRepository.save(user);

            musicFeign.createDefaultSonglist(user);

            musicFeign.createPlaylist(user.getUserId());

            log.info("Successfully registered user: {}", user.getUserId());
            return true;
        } catch (Exception e) {
            log.error("Failed to register user with phone {}: {}", user.getPhone(), e.getMessage());
            throw e;
        }
    }

    @Override
    public String login(String phone, String password) {
        try {
            password = DigestUtils.md5DigestAsHex((password + "TuneIsland").getBytes());
            User user = userRepository.findByPhoneAndPassword(phone, password);
            if (user == null) {
                throw TuneIslandException.phoneOrPasswordError();
            }
            String token = tokenUtil.getToken(user);
            log.info("User {} successfully logged in", user.getUserId());
            return token;
        } catch (Exception e) {
            log.error("Login failed for phone {}: {}", phone, e.getMessage());
            throw e;
        }
    }

    @Override
    public Boolean updateUser(User user) {
        try {
            User currentUser = tokenUtil.getCurrentUser();
            if (!currentUser.getPassword().equals(user.getPassword())) {
                user.setPassword(DigestUtils.md5DigestAsHex((user.getPassword() + "TuneIsland").getBytes()));
            }

            musicFeign.updateMyLikeSonglistName(user);

            userRepository.save(user);
            log.info("Successfully updated user: {}", user.getUserId());
            return true;
        } catch (Exception e) {
            log.error("Failed to update user {}: {}", user.getUserId(), e.getMessage());
            throw e;
        }
    }


    @Override
    public Boolean verifyPwd(String password) {
        User user = tokenUtil.getCurrentUser();
        return user.getPassword().equals(DigestUtils.md5DigestAsHex((password + "TuneIsland").getBytes()));
    }
}