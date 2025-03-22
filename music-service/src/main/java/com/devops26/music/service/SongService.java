package com.devops26.music.service;

import java.util.List;

import com.devops26.music.entity.Song;

public interface SongService {
    Integer uploadSong(Song song);
    Song getSongById(Integer songId);
    List<Song> getAllSongs();
    Boolean updateSong(Song song);
    List<Song> searchSongsByName(String name);
    List<Song> searchSongsBySinger(String singer);
    Boolean play(Integer songId);
    List<Song> searchSongs(String keyword);
    List<Song> getSongsByTag(String tag);
    List<Song> getHotSongs();
    List<Song> getListByTag(String tag);
    Boolean rateSong(Integer songId, Double rate);
    Boolean collectSong(Integer songId, Integer songlistId);
    Boolean likeSong(Integer songId);
    Boolean cancelLikeSong(Integer songId);
    List<Song> getRecommendedSongs(Integer userId, Integer numRecommendations);
    void trainRecommendationModel();
    default List<Song> getRecommendedSongs(Integer userId) {
        return getRecommendedSongs(userId, 10);
    }
}