package com.devops26.music.service.impl;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import com.devops26.music.enums.PlayStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devops26.music.entity.Playlist;
import com.devops26.music.entity.Song;
import com.devops26.music.exception.TuneIslandException;
import com.devops26.music.repository.PlaylistRepository;
import com.devops26.music.repository.SongRepository;
import com.devops26.music.service.PlaylistService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PlaylistServiceImpl implements PlaylistService {
    @Autowired
    PlaylistRepository playlistRepository;

    @Override
    public Playlist getPlaylistByUserId(Integer userId) {
        return playlistRepository.findByUserId(userId);
    }

    @Override
    public Boolean updatePlaylist(Playlist playlist) {
        validatePlaylistUpdate(playlist);
        synchronized (("playlist:" + playlist.getPlaylistId()).intern()) {
            return executePlaylistUpdate(playlist);
        }
    }

    private void validatePlaylistUpdate(Playlist playlist) {
        Playlist existingPlaylist = playlistRepository.findByUserId(playlist.getUserId());
        if (existingPlaylist == null || !Objects.equals(existingPlaylist.getPlaylistId(), playlist.getPlaylistId())) {
            throw TuneIslandException.playlistAlreadyExists();
        }
    }

    private Boolean executePlaylistUpdate(Playlist playlist) {
        int retries = 3;
        while (retries > 0) {
            try {
                playlistRepository.save(playlist);
                log.info("Successfully updated playlist for user: {}", playlist.getUserId());
                return true;
            } catch (Exception e) {
                if (--retries == 0) {
                    log.error("Failed to update playlist for user {} after 3 retries: {}",
                            playlist.getUserId(), e.getMessage());
                    throw e;
                }
                handleRetry(playlist, retries);
            }
        }
        return false;
    }

    private void handleRetry(Playlist playlist, int remainingRetries) {
        log.warn("Retrying update playlist for user {}, {} attempts remaining",
                playlist.getUserId(), remainingRetries);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Integer createPlaylist(Integer userId) {
        validateNewPlaylist(userId);
        return saveNewPlaylist(userId);
    }

    private void validateNewPlaylist(Integer userId) {
        if (playlistRepository.findByUserId(userId) != null) {
            throw TuneIslandException.playlistAlreadyExists();
        }
    }

    private Integer saveNewPlaylist(Integer userId) {
        try {
            Playlist playlist = createDefaultPlaylist(userId);
            playlist = playlistRepository.save(playlist);
            log.info("Successfully created playlist for user: {}", userId);
            return playlist.getPlaylistId();
        } catch (Exception e) {
            log.error("Failed to create playlist for user {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    private Playlist createDefaultPlaylist(Integer userId) {
        Playlist playlist = new Playlist();
        playlist.setUserId(userId);
        playlist.setPlayStrategy(PlayStrategy.ORDER);
        return playlist;
    }
}