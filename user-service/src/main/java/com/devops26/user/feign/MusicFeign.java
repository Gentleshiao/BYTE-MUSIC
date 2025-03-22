package com.devops26.user.feign;

import com.devops26.user.config.FeignConfig;
import com.devops26.user.entity.ResultVO;
import com.devops26.user.entity.Songlist;
import com.devops26.user.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "music-service", configuration = FeignConfig.class)
public interface MusicFeign {

    @PostMapping("/playlist/createPlaylist")
    ResultVO<Integer> createPlaylist(@RequestParam("userId") Integer userId);

    @PostMapping("/songlist/createDefaultSonglist")
    ResultVO<Boolean> createDefaultSonglist(@RequestBody User user);

    @PostMapping("/songlist/updateMylikeSonglistName")
    ResultVO<Boolean> updateMyLikeSonglistName(@RequestBody User user);
}
