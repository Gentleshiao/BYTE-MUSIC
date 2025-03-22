package com.devops26.music.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.devops26.music.entity.Playlist;

public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {
    Playlist findByPlaylistId(Integer playlistId);
    
    Playlist findByUserId(Integer userId);
} 