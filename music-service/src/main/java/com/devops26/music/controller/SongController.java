package com.devops26.music.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.devops26.music.entity.ResultVO;
import com.devops26.music.entity.Song;
import com.devops26.music.service.SongService;

@RestController
@RequestMapping("/songs")
public class SongController {

    @Autowired
    private SongService songService;

    @PostMapping("/uploadSong")
    public ResultVO<Integer> uploadSong(@RequestBody Song song) {
        return ResultVO.buildSuccess(songService.uploadSong(song));
    }

    @PostMapping("/updateSong")
    public ResultVO<Boolean> updateSong(@RequestBody Song song) {
        return ResultVO.buildSuccess(songService.updateSong(song));
    }

    @GetMapping("/getSongById")
    public ResultVO<Song> getSongById(@RequestParam(name = "songId") Integer songId) {
        return ResultVO.buildSuccess(songService.getSongById(songId));
    }

    @GetMapping("/getAllSongs")
    public ResultVO<List<Song>> getAllSongs() {
        return ResultVO.buildSuccess(songService.getAllSongs());
    }

    @GetMapping("/searchByName")
    public ResultVO<List<Song>> searchSongsByName(@RequestParam(name = "name") String name) {
        return ResultVO.buildSuccess(songService.searchSongsByName(name));
    }

    @GetMapping("/searchBySinger")
    public ResultVO<List<Song>> searchSongsBySinger(@RequestParam(name = "singer") String singer) {
        return ResultVO.buildSuccess(songService.searchSongsBySinger(singer));
    }

    @GetMapping("/search")
    public ResultVO<List<Song>> searchSongs(@RequestParam(name = "keyword") String keyword) {
        return ResultVO.buildSuccess(songService.searchSongs(keyword));
    }

    @GetMapping("/getByTag")
    public ResultVO<List<Song>> getSongsByTag(@RequestParam(name = "tag") String tag) {
        return ResultVO.buildSuccess(songService.getSongsByTag(tag));
    }

    //热歌榜
    @GetMapping("/hotSongs")
    public ResultVO<List<Song>> getHotSongs() {
        return ResultVO.buildSuccess(songService.getHotSongs());
    }

    //流行音乐榜
    @GetMapping("/getListByTag")
    public ResultVO<List<Song>> getListByTag(@RequestParam(name = "tag") String tag) {
        return ResultVO.buildSuccess(songService.getListByTag(tag));
    }

    @PostMapping("/rateSong")
    public ResultVO<Boolean> rateSong(@RequestParam(name = "songId") Integer songId, @RequestParam(name="rate") Double rate) {
        return ResultVO.buildSuccess(songService.rateSong(songId, rate));
    }

    @PostMapping("/play")
    public ResultVO<Boolean> play(@RequestParam(name = "songId") Integer songId) {
        return ResultVO.buildSuccess(songService.play(songId));
    }

    @PostMapping("/collectSong")
    public ResultVO<Boolean> collectSong(@RequestParam(name = "songId") Integer songId, @RequestParam(name = "songlistId") Integer songlistId) {
        return ResultVO.buildSuccess(songService.collectSong(songId, songlistId));
    }

    @PostMapping("/likeSong")
    public ResultVO<Boolean> likeSong(@RequestParam("songId") Integer songId) {
        return ResultVO.buildSuccess(songService.likeSong(songId));
    }

    @PostMapping("/cancelLikeSong")
    public ResultVO<Boolean> cancelLikeSong(@RequestParam("songId") Integer songId) {
        return ResultVO.buildSuccess(songService.cancelLikeSong(songId));
    }

    @GetMapping("/recommendations")
    public ResultVO<List<Song>> getRecommendations(@RequestParam(name = "userId") Integer userId) {
        List<Song> recommendations = songService.getRecommendedSongs(userId, 10);
        return ResultVO.buildSuccess(recommendations);
    }

}