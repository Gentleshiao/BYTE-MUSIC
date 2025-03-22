package com.devops26.music.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.devops26.music.entity.Playlist;
import com.devops26.music.enums.PlayStrategy;
import com.devops26.music.exception.TuneIslandException;
import com.devops26.music.repository.PlaylistRepository;
import com.devops26.music.repository.SongRepository;

class PlaylistServiceImplTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private PlaylistServiceImpl playlistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getPlaylistByUserId_Success() {
        Playlist playlist = new Playlist();
        playlist.setUserId(1);
        playlist.setPlayStrategy(PlayStrategy.ORDER);
        playlist.setSongs(new ArrayList<>());

        when(playlistRepository.findByUserId(1)).thenReturn(playlist);

        Playlist result = playlistService.getPlaylistByUserId(1);

        assertNotNull(result);
        assertEquals(1, result.getUserId());
        assertEquals(PlayStrategy.ORDER, result.getPlayStrategy());
    }

    @Test
    void getPlaylistByUserId_NotFound() {
        when(playlistRepository.findByUserId(1)).thenReturn(null);

        Playlist result = playlistService.getPlaylistByUserId(1);

        assertNull(result);
    }

    @Test
    void updatePlaylist_Success() {
        Playlist playlist = new Playlist();
        playlist.setPlaylistId(1);
        playlist.setUserId(1);
        playlist.setPlayStrategy(PlayStrategy.ORDER);
        List<Integer> songs = Arrays.asList(1, 2, 3);
        playlist.setSongs(songs);

        when(playlistRepository.findByUserId(anyInt())).thenReturn(playlist);
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

        Boolean result = playlistService.updatePlaylist(playlist);
        
        assertTrue(result);
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    void updatePlaylist_WithRandomStrategy() {
        Playlist playlist = new Playlist();
        playlist.setPlaylistId(1);
        playlist.setUserId(1);
        playlist.setPlayStrategy(PlayStrategy.RANDOM);
        List<Integer> songs = Arrays.asList(1, 2, 3);
        playlist.setSongs(songs);

        when(playlistRepository.findByUserId(anyInt())).thenReturn(playlist);
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

        Boolean result = playlistService.updatePlaylist(playlist);
        
        assertTrue(result);
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    void updatePlaylist_WithSingleStrategy() {
        Playlist playlist = new Playlist();
        playlist.setPlaylistId(1);
        playlist.setUserId(1);
        playlist.setPlayStrategy(PlayStrategy.SINGLE);
        List<Integer> songs = Arrays.asList(1, 2, 3);
        playlist.setSongs(songs);

        when(playlistRepository.findByUserId(anyInt())).thenReturn(playlist);
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

        Boolean result = playlistService.updatePlaylist(playlist);
        
        assertTrue(result);
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    void updatePlaylist_NotFound() {
        Playlist playlist = new Playlist();
        playlist.setPlaylistId(1);
        playlist.setUserId(1);

        when(playlistRepository.findByUserId(anyInt())).thenReturn(null);

        assertThrows(TuneIslandException.class, () -> playlistService.updatePlaylist(playlist));
    }

    @Test
    void updatePlaylist_PlaylistIdMismatch() {
        Playlist existingPlaylist = new Playlist();
        existingPlaylist.setPlaylistId(1);
        existingPlaylist.setUserId(1);
        existingPlaylist.setPlayStrategy(PlayStrategy.ORDER);
        existingPlaylist.setSongs(new ArrayList<>());

        Playlist newPlaylist = new Playlist();
        newPlaylist.setPlaylistId(2);
        newPlaylist.setUserId(1);
        newPlaylist.setPlayStrategy(PlayStrategy.ORDER);
        newPlaylist.setSongs(new ArrayList<>());

        when(playlistRepository.findByUserId(anyInt())).thenReturn(existingPlaylist);

        assertThrows(TuneIslandException.class, () -> playlistService.updatePlaylist(newPlaylist));
    }

    @Test
    void updatePlaylist_SaveError() {
        Playlist playlist = new Playlist();
        playlist.setPlaylistId(1);
        playlist.setUserId(1);
        playlist.setPlayStrategy(PlayStrategy.ORDER);
        List<Integer> songs = Arrays.asList(1, 2, 3);
        playlist.setSongs(songs);

        when(playlistRepository.findByUserId(anyInt())).thenReturn(playlist);
        when(playlistRepository.save(any(Playlist.class))).thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class, () -> playlistService.updatePlaylist(playlist));
    }

    @Test
    void createPlaylist_Success() {
        Playlist playlist = new Playlist();
        playlist.setPlaylistId(1);
        playlist.setUserId(1);
        playlist.setPlayStrategy(PlayStrategy.ORDER);
        playlist.setSongs(new ArrayList<>());
        
        when(playlistRepository.findByUserId(anyInt())).thenReturn(null);
        when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

        Integer result = playlistService.createPlaylist(1);
        
        assertNotNull(result);
        assertEquals(1, result);
        verify(playlistRepository).save(any(Playlist.class));
    }

    @Test
    void createPlaylist_AlreadyExists() {
        Playlist playlist = new Playlist();
        playlist.setPlaylistId(1);
        playlist.setUserId(1);
        
        when(playlistRepository.findByUserId(anyInt())).thenReturn(playlist);

        assertThrows(TuneIslandException.class, () -> playlistService.createPlaylist(1));
    }

    @Test
    void createPlaylist_SaveError() {
        when(playlistRepository.findByUserId(anyInt())).thenReturn(null);
        when(playlistRepository.save(any(Playlist.class))).thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class, () -> playlistService.createPlaylist(1));
    }
}