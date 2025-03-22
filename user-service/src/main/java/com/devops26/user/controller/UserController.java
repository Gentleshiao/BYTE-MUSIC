package com.devops26.user.controller;

import com.devops26.user.entity.ResultVO;
import com.devops26.user.repository.UserRepository;
import com.devops26.user.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devops26.user.entity.User;
import com.devops26.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TokenUtil tokenUtil;

    @GetMapping("/getUserById")
    public ResultVO<User> getUserById(@RequestParam("userId") Integer userId) {
        return ResultVO.buildSuccess(userService.findByUserId(userId));
    }

    @PostMapping("/register")
    public ResultVO<Boolean> register(@RequestBody User user) {
        return ResultVO.buildSuccess(userService.register(user));
    }

    @PostMapping("/login")
    public ResultVO<String> login(@RequestParam("phone") String phone, @RequestParam("password") String password) {
        return ResultVO.buildSuccess(userService.login(phone, password));
    }

    @GetMapping("/getCurrentUser")
    public ResultVO<User> getCurrentUser() {
        return ResultVO.buildSuccess(tokenUtil.getCurrentUser());
    }

    @PostMapping("/updateUser")
    public ResultVO<Boolean> updateUser(@RequestBody User user) {
        return ResultVO.buildSuccess(userService.updateUser(user));
    }

    @GetMapping("verifyPwd")
    public ResultVO<Boolean> verifyPwd(@RequestParam("password") String password) {
        return ResultVO.buildSuccess(userService.verifyPwd(password));
    }

    @PostMapping("/save")
    public ResultVO<User> save(@RequestBody User user) {
        return ResultVO.buildSuccess(userRepository.save(user));
    }

    @GetMapping("/findAll")
    public ResultVO<List<User>> findAll() {
        return ResultVO.buildSuccess(userRepository.findAll());
    }

    @PostMapping("/saveAll")
    public ResultVO<List<User>> saveAll(@RequestBody List<User> users) {
        return ResultVO.buildSuccess(userRepository.saveAll(users));
    }

    @PostMapping("/verifyToken")
    public ResultVO<Boolean> verifyToken(@RequestParam("token") String token) {
        return ResultVO.buildSuccess(tokenUtil.verifyToken(token));
    }
}
