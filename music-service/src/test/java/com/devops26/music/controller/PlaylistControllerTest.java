package com.devops26.music.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.devops26.music.entity.Playlist;
import com.devops26.music.entity.ResultVO;
import com.devops26.music.service.PlaylistService;

class PlaylistControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PlaylistService playlistService;

    @InjectMocks
    private PlaylistController playlistController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(playlistController).build();
    }

    @Test
    void createPlaylist_ShouldReturnSuccess() {
        when(playlistService.createPlaylist(1)).thenReturn(1);

        ResultVO<Integer> result = playlistController.createPlaylist(1);
        assertEquals("000", result.getCode());
        assertEquals(1, result.getResult());
    }

    @Test
    void getPlaylistByUserId_ShouldReturnPlaylist() {
        Playlist playlist = new Playlist();
        when(playlistService.getPlaylistByUserId(1)).thenReturn(playlist);

        ResultVO<Playlist> result = playlistController.getPlaylistByUserId(1);
        assertEquals("000", result.getCode());
        assertNotNull(result.getResult());
    }

    @Test
    void updatePlaylist_ShouldReturnSuccess() {
        Playlist playlist = new Playlist();
        when(playlistService.updatePlaylist(any(Playlist.class))).thenReturn(true);

        ResultVO<Boolean> result = playlistController.updatePlaylist(playlist);
        assertEquals("000", result.getCode());
        assertTrue(result.getResult());
    }
} 