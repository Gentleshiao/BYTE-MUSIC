package com.devops26.music.controller;

import java.util.Arrays;
import java.util.List;

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
import com.devops26.music.entity.Song;
import com.devops26.music.service.SongService;

class SongControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SongService songService;

    @InjectMocks
    private SongController songController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(songController).build();
    }

    @Test
    void uploadSong_ShouldReturnSuccess() {
        Song song = new Song();
        song.setSongId(1);
        when(songService.uploadSong(any(Song.class))).thenReturn(1);

        ResultVO<Integer> result = songController.uploadSong(song);
        assert result.getCode().equals("000");
        assert result.getResult().equals(1);
    }

    @Test
    void updateSong_ShouldReturnSuccess() {
        Song song = new Song();
        when(songService.updateSong(any(Song.class))).thenReturn(true);

        ResultVO<Boolean> result = songController.updateSong(song);
        assert result.getCode().equals("000");
        assert result.getResult();
    }

    @Test
    void getSongById_ShouldReturnSong() {
        Song song = new Song();
        song.setSongId(1);
        when(songService.getSongById(1)).thenReturn(song);

        ResultVO<Song> result = songController.getSongById(1);
        assert result.getCode().equals("000");
        assert result.getResult().getSongId().equals(1);
    }

    @Test
    void getAllSongs_ShouldReturnSongList() {
        List<Song> songs = Arrays.asList(new Song(), new Song());
        when(songService.getAllSongs()).thenReturn(songs);

        ResultVO<List<Song>> result = songController.getAllSongs();
        assert result.getCode().equals("000");
        assert result.getResult().size() == 2;
    }

    @Test
    void searchByName_ShouldReturnSongList() {
        List<Song> songs = Arrays.asList(new Song(), new Song());
        when(songService.searchSongsByName("test")).thenReturn(songs);

        ResultVO<List<Song>> result = songController.searchSongsByName("test");
        assert result.getCode().equals("000");
        assert result.getResult().size() == 2;
    }

    @Test
    void searchBySinger_ShouldReturnSongList() {
        List<Song> songs = Arrays.asList(new Song(), new Song());
        when(songService.searchSongsBySinger("singer")).thenReturn(songs);

        ResultVO<List<Song>> result = songController.searchSongsBySinger("singer");
        assert result.getCode().equals("000");
        assert result.getResult().size() == 2;
    }

    @Test
    void search_ShouldReturnSongList() {
        List<Song> songs = Arrays.asList(new Song(), new Song());
        when(songService.searchSongs("keyword")).thenReturn(songs);

        ResultVO<List<Song>> result = songController.searchSongs("keyword");
        assert result.getCode().equals("000");
        assert result.getResult().size() == 2;
    }

    @Test
    void getByTag_ShouldReturnSongList() {
        List<Song> songs = Arrays.asList(new Song(), new Song());
        when(songService.getSongsByTag("POP")).thenReturn(songs);

        ResultVO<List<Song>> result = songController.getSongsByTag("POP");
        assert result.getCode().equals("000");
        assert result.getResult().size() == 2;
    }

    @Test
    void rateSong_ShouldReturnSuccess() {
        when(songService.rateSong(1, 4.5)).thenReturn(true);

        ResultVO<Boolean> result = songController.rateSong(1, 4.5);
        assert result.getCode().equals("000");
        assert result.getResult();
    }

    @Test
    void play_ShouldReturnSuccess() {
        when(songService.play(1)).thenReturn(true);

        ResultVO<Boolean> result = songController.play(1);
        assert result.getCode().equals("000");
        assert result.getResult();
    }

    @Test
    void collectSong_ShouldReturnSuccess() {
        when(songService.collectSong(1, 1)).thenReturn(true);

        ResultVO<Boolean> result = songController.collectSong(1, 1);
        assert result.getCode().equals("000");
        assert result.getResult();
    }

    @Test
    void likeSong_ShouldReturnSuccess() {
        when(songService.likeSong(1)).thenReturn(true);

        ResultVO<Boolean> result = songController.likeSong(1);
        assert result.getCode().equals("000");
        assert result.getResult();
    }

    @Test
    void cancelLikeSong_ShouldReturnSuccess() {
        when(songService.cancelLikeSong(1)).thenReturn(true);

        ResultVO<Boolean> result = songController.cancelLikeSong(1);
        assert result.getCode().equals("000");
        assert result.getResult();
    }

    @Test
    void getRecommendations_ShouldReturnSongList() {
        List<Song> songs = Arrays.asList(new Song(), new Song());
        when(songService.getRecommendedSongs(1, 10)).thenReturn(songs);

        ResultVO<List<Song>> result = songController.getRecommendations(1);
        assert result.getCode().equals("000");
        assert result.getResult().size() == 2;
    }
} 