package com.devops26.music.controller;

import java.util.Arrays;
import java.util.List;

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

import com.devops26.music.entity.ResultVO;
import com.devops26.music.entity.Songlist;
import com.devops26.music.entity.User;
import com.devops26.music.service.SonglistService;

class SonglistControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SonglistService songlistService;

    @InjectMocks
    private SonglistController songlistController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(songlistController).build();
    }

    @Test
    void createSonglist_ShouldReturnSuccess() {
        Songlist songlist = new Songlist();
        when(songlistService.createSonglist(any(Songlist.class))).thenReturn(1);

        ResultVO<Integer> result = songlistController.createSonglist(songlist);
        assertEquals("000", result.getCode());
        assertEquals(1, result.getResult());
    }

    @Test
    void deleteSonglist_ShouldReturnSuccess() {
        when(songlistService.deleteSonglist(1)).thenReturn(true);

        ResultVO<Boolean> result = songlistController.deleteSonglist(1);
        assertEquals("000", result.getCode());
        assertTrue(result.getResult());
    }

    @Test
    void updateSonglist_ShouldReturnSuccess() {
        Songlist songlist = new Songlist();
        when(songlistService.updateSonglist(any(Songlist.class))).thenReturn(true);

        ResultVO<Boolean> result = songlistController.updateSonglist(songlist);
        assertEquals("000", result.getCode());
        assertTrue(result.getResult());
    }

    @Test
    void getMylikeSonglist_ShouldReturnSonglist() {
        Songlist songlist = new Songlist();
        when(songlistService.getMylikeSonglist(1)).thenReturn(songlist);

        ResultVO<Songlist> result = songlistController.getMylikeSonglist(1);
        assertEquals("000", result.getCode());
        assertNotNull(result.getResult());
    }

    @Test
    void getAllByOwnerId_ShouldReturnSonglists() {
        List<Songlist> songlists = Arrays.asList(new Songlist(), new Songlist());
        when(songlistService.getAllByOwnerId(1)).thenReturn(songlists);

        ResultVO<List<Songlist>> result = songlistController.getAllByOwnerId(1);
        assertEquals("000", result.getCode());
        assertEquals(2, result.getResult().size());
    }

    @Test
    void getBySonglistId_ShouldReturnSonglist() {
        Songlist songlist = new Songlist();
        when(songlistService.getBySonglistId(1)).thenReturn(songlist);

        ResultVO<Songlist> result = songlistController.getBySonglistId(1);
        assertEquals("000", result.getCode());
        assertNotNull(result.getResult());
    }

    @Test
    void getByName_ShouldReturnSonglist() {
        Songlist songlist = new Songlist();
        when(songlistService.getByName("test")).thenReturn(songlist);

        ResultVO<Songlist> result = songlistController.getByName("test");
        assertEquals("000", result.getCode());
        assertNotNull(result.getResult());
    }

    @Test
    void collectSonglist_ShouldReturnSuccess() {
        when(songlistService.collectSonglist(1)).thenReturn(1);

        ResultVO<Integer> result = songlistController.collectSonglist(1);
        assertEquals("000", result.getCode());
        assertEquals(1, result.getResult());
    }

    @Test
    void rate_ShouldReturnSuccess() {
        when(songlistService.rate(1, 4.5)).thenReturn(4.5);

        ResultVO<Double> result = songlistController.rate(1, 4.5);
        assertEquals("000", result.getCode());
        assertEquals(4.5, result.getResult());
    }

    @Test
    void getPublicSonglists_ShouldReturnSonglists() {
        List<Songlist> songlists = Arrays.asList(new Songlist(), new Songlist());
        when(songlistService.getPublicSonglists()).thenReturn(songlists);

        ResultVO<List<Songlist>> result = songlistController.getPublicSonglists();
        assertEquals("000", result.getCode());
        assertEquals(2, result.getResult().size());
    }

    @Test
    void cancelCollectSonglist_ShouldReturnSuccess() {
        when(songlistService.cancelCollectSonglist(1)).thenReturn(true);

        ResultVO<Boolean> result = songlistController.cancelCollectSonglist(1);
        assertEquals("000", result.getCode());
        assertTrue(result.getResult());
    }

    @Test
    void getRecommendations_ShouldReturnSonglists() {
        List<Songlist> songlists = Arrays.asList(new Songlist(), new Songlist());
        when(songlistService.getRecommendedSonglists()).thenReturn(songlists);

        ResultVO<List<Songlist>> result = songlistController.getRecommendations();
        assertEquals("000", result.getCode());
        assertEquals(2, result.getResult().size());
    }

    @Test
    void createDefaultSonglist_ShouldReturnSuccess() {
        User user = new User();
        when(songlistService.createDefaultSonglist(any(User.class))).thenReturn(true);

        ResultVO<Boolean> result = songlistController.createDefaultSonglist(user);
        assertEquals("000", result.getCode());
        assertTrue(result.getResult());
    }

    @Test
    void updateMylikeSonglistName_ShouldReturnSuccess() {
        User user = new User();
        when(songlistService.updateMyLikeSonglistName(any(User.class))).thenReturn(true);

        ResultVO<Boolean> result = songlistController.updateMyLikeSonglistName(user);
        assertEquals("000", result.getCode());
        assertTrue(result.getResult());
    }
} 