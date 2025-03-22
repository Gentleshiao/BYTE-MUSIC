package com.devops26.user.controller;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.devops26.user.entity.User;
import com.devops26.user.exception.TuneIslandException;
import com.devops26.user.repository.UserRepository;
import com.devops26.user.service.UserService;
import com.devops26.user.util.TokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private TokenUtil tokenUtil;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1);
        testUser.setPhone("13800138000");
        testUser.setPassword("password123");
        testUser.setName("测试用户");
    }


    @Test
    void register_ShouldReturnSuccess() throws Exception {
        when(userService.register(any(User.class))).thenReturn(true);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    void register_ShouldReturnError_WhenPhoneExists() throws Exception {
        when(userService.register(any(User.class))).thenThrow(TuneIslandException.phoneAlreadyExists());

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.msg").value("手机号已经被注册过了!"));
    }

    @Test
    void login_ShouldReturnToken() throws Exception {
        String token = "test.jwt.token";
        when(userService.login(anyString(), anyString())).thenReturn(token);

        mockMvc.perform(post("/user/login")
                        .param("phone", testUser.getPhone())
                        .param("password", testUser.getPassword())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result").value(token));
    }

    @Test
    void login_ShouldReturnError_WhenInvalidCredentials() throws Exception {
        when(userService.login(anyString(), anyString())).thenThrow(TuneIslandException.phoneOrPasswordError());

        mockMvc.perform(post("/user/login")
                        .param("phone", "wrong_phone")
                        .param("password", "wrong_password")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.msg").value("手机号或密码错误!"));
    }

    @Test
    void updateUser_ShouldReturnSuccess() throws Exception {
        when(userService.updateUser(any(User.class))).thenReturn(true);

        mockMvc.perform(post("/user/updateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    void getCurrentUser_ShouldReturnUser() throws Exception {
        when(tokenUtil.getCurrentUser()).thenReturn(testUser);

        mockMvc.perform(get("/user/getCurrentUser"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result.userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.result.name").value(testUser.getName()));
    }

    @Test
    void getCurrentUser_ShouldReturnNull_WhenNotLoggedIn() throws Exception {
        when(tokenUtil.getCurrentUser()).thenReturn(null);

        mockMvc.perform(get("/user/getCurrentUser"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result").isEmpty());
    }

    @Test
    void verifyPwd_ShouldReturnTrue() throws Exception {
        when(userService.verifyPwd(anyString())).thenReturn(true);

        mockMvc.perform(get("/user/verifyPwd")
                .param("password", "password123"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    void verifyPwd_ShouldReturnFalse() throws Exception {
        when(userService.verifyPwd(anyString())).thenReturn(false);

        mockMvc.perform(get("/user/verifyPwd")
                .param("password", "wrongpassword"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result").value(false));
    }

    @Test
    void save_ShouldReturnSavedUser() throws Exception {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        mockMvc.perform(post("/user/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testUser)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result.userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.result.name").value(testUser.getName()));
    }

    @Test
    void findAll_ShouldReturnAllUsers() throws Exception {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        mockMvc.perform(get("/user/findAll"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result[0].userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.result[0].name").value(testUser.getName()));
    }

    @Test
    void saveAll_ShouldReturnSavedUsers() throws Exception {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.saveAll(any())).thenReturn(users);

        mockMvc.perform(post("/user/saveAll")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(users)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result[0].userId").value(testUser.getUserId()))
                .andExpect(jsonPath("$.result[0].name").value(testUser.getName()));
    }

    @Test
    void verifyToken_ShouldReturnTrue() throws Exception {
        when(tokenUtil.verifyToken(anyString())).thenReturn(true);

        mockMvc.perform(post("/user/verifyToken")
                .param("token", "valid.jwt.token"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    void verifyToken_ShouldReturnFalse() throws Exception {
        when(tokenUtil.verifyToken(anyString())).thenReturn(false);

        mockMvc.perform(post("/user/verifyToken")
                .param("token", "invalid.jwt.token"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result").value(false));
    }
}