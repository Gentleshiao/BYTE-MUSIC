package com.devops26.music.service;

import com.devops26.music.entity.Playlist;

public interface PlaylistService {
    Playlist getPlaylistByUserId(Integer userId);
    Boolean updatePlaylist(Playlist playlist);
    Integer createPlaylist(Integer userId);
} 