package com.devops26.music.controller;

import com.devops26.music.entity.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devops26.music.entity.Playlist;
import com.devops26.music.service.PlaylistService;

@RestController
@RequestMapping("/playlist")
public class PlaylistController {
    @Autowired
    PlaylistService playlistService;

    @PostMapping("/createPlaylist")
    public ResultVO<Integer> createPlaylist(@RequestParam(name = "userId") Integer userId) {
        return ResultVO.buildSuccess(playlistService.createPlaylist(userId));
    }

    @GetMapping("/getPlaylistByUserId")
    public ResultVO<Playlist> getPlaylistByUserId(@RequestParam(name = "userId") Integer userId) {
        return ResultVO.buildSuccess(playlistService.getPlaylistByUserId(userId));
    }

    @PostMapping("/updatePlaylist")
    public ResultVO<Boolean> updatePlaylist(@RequestBody Playlist playlist) {
        return ResultVO.buildSuccess(playlistService.updatePlaylist(playlist));
    }
}