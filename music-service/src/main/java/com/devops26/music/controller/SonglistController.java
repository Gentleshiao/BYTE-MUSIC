package com.devops26.music.controller;

import java.util.List;

import com.devops26.music.entity.ResultVO;
import com.devops26.music.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devops26.music.entity.Songlist;
import com.devops26.music.service.SonglistService;

@RestController
@RequestMapping("/songlist")
public class SonglistController {
    @Autowired
    SonglistService songlistService;

    @PostMapping("/createSonglist")
    public ResultVO<Integer> createSonglist(@RequestBody Songlist songlist) {
        return ResultVO.buildSuccess(songlistService.createSonglist(songlist));
    }

    @PostMapping("/deleteSonglist")
    public ResultVO<Boolean> deleteSonglist(@RequestParam(name = "songlistId") Integer songlistId) {
        return ResultVO.buildSuccess(songlistService.deleteSonglist(songlistId));
    }

    @PostMapping("/updateSonglist")
    public ResultVO<Boolean> updateSonglist(@RequestBody Songlist songlist) {
        return ResultVO.buildSuccess(songlistService.updateSonglist(songlist));
    }

    @GetMapping("/getMylikeSonglist")
    public ResultVO<Songlist> getMylikeSonglist(@RequestParam(name = "userId") Integer userId) {
        return ResultVO.buildSuccess(songlistService.getMylikeSonglist(userId));
    }

    @GetMapping("/getAllByOwnerId")
    public ResultVO<List<Songlist>> getAllByOwnerId(@RequestParam(name = "ownerId") Integer ownerId) {
        return ResultVO.buildSuccess(songlistService.getAllByOwnerId(ownerId));
    }

    @GetMapping("/getBySonglistId")
    public ResultVO<Songlist> getBySonglistId(@RequestParam(name = "songlistId") Integer songlistId) {
        return ResultVO.buildSuccess(songlistService.getBySonglistId(songlistId));
    }

    @GetMapping("/getByName")
    public ResultVO<Songlist> getByName(@RequestParam(name = "name") String name) {
        return ResultVO.buildSuccess(songlistService.getByName(name));
    }

    @PostMapping("/collectSonglist")
    public ResultVO<Integer> collectSonglist(@RequestParam(name = "songlistId") Integer songlistId) {
        return ResultVO.buildSuccess(songlistService.collectSonglist(songlistId));
    }

    @PostMapping("/rate")
    public ResultVO<Double> rate(@RequestParam(name = "songlistId") Integer songlistId, @RequestParam(name = "rate") Double rate) {
        return ResultVO.buildSuccess(songlistService.rate(songlistId, rate));
    }

    @GetMapping("/getPublicSonglists")
    public ResultVO<List<Songlist>> getPublicSonglists() {
        return ResultVO.buildSuccess(songlistService.getPublicSonglists());
    }

    @PostMapping("/cancelCollectSonglist")
    public ResultVO<Boolean> cancelCollectSonglist(@RequestParam(name = "songlistId") Integer songlistId) {
        return ResultVO.buildSuccess(songlistService.cancelCollectSonglist(songlistId));
    }

    @GetMapping("/getRecommendations")
    public ResultVO<List<Songlist>> getRecommendations() {
        return ResultVO.buildSuccess(songlistService.getRecommendedSonglists());
    }

    @PostMapping("/createDefaultSonglist")
    public ResultVO<Boolean> createDefaultSonglist(@RequestBody User user) {
        return ResultVO.buildSuccess(songlistService.createDefaultSonglist(user));
    }

    @PostMapping("/updateMylikeSonglistName")
    public ResultVO<Boolean> updateMyLikeSonglistName(@RequestBody User user) {
        return ResultVO.buildSuccess(songlistService.updateMyLikeSonglistName(user));
    }
}
