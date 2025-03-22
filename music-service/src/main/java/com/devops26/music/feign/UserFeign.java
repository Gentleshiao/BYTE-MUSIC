package com.devops26.music.feign;

import com.devops26.music.config.FeignConfig;
import com.devops26.music.entity.ResultVO;
import com.devops26.music.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserFeign {
    @GetMapping("/user/getUserById")
    ResultVO<User> getUserById(@RequestParam("userId") Integer userId);

    @PostMapping("/user/save")
    ResultVO<User> save(@RequestBody User user);

    @GetMapping("/user/getCurrentUser")
    ResultVO<User> getCurrentUser();

    @GetMapping("/user/findAll")
    ResultVO<List<User>> findAll();

    @PostMapping("/user/saveAll")
    ResultVO<List<User>> saveAll(@RequestBody List<User> users);
}
