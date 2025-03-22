package com.devops26.music.service;

import java.util.List;

import com.devops26.music.entity.Songlist;
import com.devops26.music.entity.User;

public interface SonglistService {
    Integer createSonglist(Songlist songlist);
    Boolean deleteSonglist(Integer songlistId);
    Boolean updateSonglist(Songlist songlist);
    Songlist getMylikeSonglist(Integer userId);
    List<Songlist> getAllByOwnerId(Integer ownerId);
    Songlist getBySonglistId(Integer songlistId);
    Songlist getByName(String name);
    Integer collectSonglist(Integer songlistId);
    Double rate(Integer songlistId, Double rate);
    List<Songlist> getPublicSonglists();
    Boolean cancelCollectSonglist(Integer songlistId);
    List<Songlist> getRecommendedSonglists();
    boolean createDefaultSonglist(User user);
    boolean updateMyLikeSonglistName(User user);
}