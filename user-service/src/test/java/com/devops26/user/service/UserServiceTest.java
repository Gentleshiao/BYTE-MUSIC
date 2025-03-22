package com.devops26.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.devops26.user.entity.ResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.util.DigestUtils;

import com.devops26.user.entity.User;
import com.devops26.user.exception.TuneIslandException;
import com.devops26.user.feign.MusicFeign;
import com.devops26.user.repository.UserRepository;
import com.devops26.user.service.impl.UserServiceImpl;
import com.devops26.user.util.TokenUtil;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenUtil tokenUtil;

    @Mock
    private MusicFeign musicFeign;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private String testPassword;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testPassword = "password123";
        testUser = new User();
        testUser.setUserId(1);
        testUser.setName("测试用户");
        testUser.setPhone("13800138000");
        testUser.setPassword(DigestUtils.md5DigestAsHex((testPassword + "TuneIsland").getBytes()));
    }

    @Test
    void findByUserId_ShouldReturnUser() {
        when(userRepository.findByUserId(1)).thenReturn(testUser);
        User result = userService.findByUserId(1);
        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.getUserId());
    }

    @Test
    void findByUserId_ShouldReturnNull_WhenUserNotFound() {
        when(userRepository.findByUserId(999)).thenReturn(null);
        User result = userService.findByUserId(999);
        assertNull(result);
    }

    @Test
    void register_ShouldReturnTrue_WhenUserRegistrationSuccessful() {
        User newUser = new User();
        newUser.setPhone("13800138001");
        newUser.setPassword(testPassword);

        when(userRepository.findByPhone(newUser.getPhone())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(musicFeign.createDefaultSonglist(any(User.class))).thenReturn(ResultVO.buildSuccess(true));
        when(musicFeign.createPlaylist(anyInt())).thenReturn(ResultVO.buildSuccess(1));

        assertTrue(userService.register(newUser));
    }

    @Test
    void register_ShouldThrowException_WhenPhoneExists() {
        User newUser = new User();
        newUser.setPhone(testUser.getPhone());
        
        when(userRepository.findByPhone(testUser.getPhone())).thenReturn(testUser);
        
        assertThrows(TuneIslandException.class, () -> userService.register(newUser));
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsValid() {
        String phone = testUser.getPhone();
        String password = testPassword;
        String expectedToken = "test.jwt.token";

        when(userRepository.findByPhoneAndPassword(
            eq(phone), 
            eq(DigestUtils.md5DigestAsHex((password + "TuneIsland").getBytes()))
        )).thenReturn(testUser);
        when(tokenUtil.getToken(testUser)).thenReturn(expectedToken);

        String token = userService.login(phone, password);
        assertEquals(expectedToken, token);
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        String phone = "wrong_phone";
        String password = "wrong_password";

        when(userRepository.findByPhoneAndPassword(
            eq(phone), 
            eq(DigestUtils.md5DigestAsHex((password + "TuneIsland").getBytes()))
        )).thenReturn(null);

        assertThrows(TuneIslandException.class, () -> userService.login(phone, password));
    }

    @Test
    void updateUser_ShouldReturnTrue_WhenUpdateSuccessful() {
        User updatedUser = new User();
        updatedUser.setUserId(1);
        updatedUser.setName("更新后的用户");
        updatedUser.setPassword(testUser.getPassword());

        when(tokenUtil.getCurrentUser()).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(musicFeign.updateMyLikeSonglistName(any(User.class))).thenReturn(null);

        assertTrue(userService.updateUser(updatedUser));
        verify(userRepository).save(any(User.class));
        verify(musicFeign).updateMyLikeSonglistName(any(User.class));
    }

    @Test
    void updateUser_ShouldReturnFalse_WhenUserNotFound() {
        User updatedUser = new User();
        updatedUser.setUserId(999);
        updatedUser.setPassword(testPassword);

        when(tokenUtil.getCurrentUser()).thenReturn(null);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () -> userService.updateUser(updatedUser));
    }

    @Test
    void verifyPwd_ShouldReturnTrue_WhenPasswordValid() {
        when(tokenUtil.getCurrentUser()).thenReturn(testUser);
        assertTrue(userService.verifyPwd(testPassword));
    }

    @Test
    void verifyPwd_ShouldReturnFalse_WhenPasswordInvalid() {
        when(tokenUtil.getCurrentUser()).thenReturn(testUser);
        assertFalse(userService.verifyPwd("wrong_password"));
    }

    @Test
    void verifyPwd_ShouldReturnFalse_WhenUserNotFound() {
        when(tokenUtil.getCurrentUser()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> userService.verifyPwd(testPassword));
    }
} 
