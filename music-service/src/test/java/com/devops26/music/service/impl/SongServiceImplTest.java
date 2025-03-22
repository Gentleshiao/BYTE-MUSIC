package com.devops26.music.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.devops26.music.entity.ResultVO;
import com.devops26.music.entity.Song;
import com.devops26.music.entity.Songlist;
import com.devops26.music.entity.User;
import com.devops26.music.enums.UserRole;
import com.devops26.music.enums.SongTag;
import com.devops26.music.exception.TuneIslandException;
import com.devops26.music.feign.UserFeign;
import com.devops26.music.repository.SongRepository;
import com.devops26.music.repository.SonglistRepository;
import com.devops26.music.service.SonglistService;
import com.devops26.music.util.MLRecommenderUtil;

class SongServiceImplTest {

    @Mock
    private UserFeign userFeign;

    @Mock
    private SonglistRepository songlistRepository;

    @Mock
    private SonglistService songlistService;

    @Mock
    private MLRecommenderUtil mlRecommenderUtil;

    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private SongServiceImpl songService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void uploadSong_Success() {
        Song song = new Song();
        song.setUrl("test.mp3");
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(admin));
        when(songRepository.findByUrl(any())).thenReturn(null);
        when(songRepository.save(any())).thenReturn(song);

        Integer result = songService.uploadSong(song);

        verify(songRepository).save(any(Song.class));
    }

    @Test
    void uploadSong_NotAdmin() {
        Song song = new Song();
        User user = new User();
        user.setRole(UserRole.USER);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));

        assertThrows(TuneIslandException.class, () -> songService.uploadSong(song));
    }

    @Test
    void uploadSong_SongExists() {
        Song song = new Song();
        song.setUrl("test.mp3");
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(admin));
        when(songRepository.findByUrl(any())).thenReturn(song);

        assertThrows(TuneIslandException.class, () -> songService.uploadSong(song));
    }

    @Test
    void getSongById_Success() {
        Song song = new Song();
        song.setSongId(1);
        
        when(songRepository.findBySongId(1)).thenReturn(song);

        Song result = songService.getSongById(1);
        
        assertNotNull(result);
        assertEquals(1, result.getSongId());
    }

    @Test
    void getAllSongs_Success() {
        List<Song> songs = Arrays.asList(new Song(), new Song());
        when(songRepository.findAll()).thenReturn(songs);

        List<Song> result = songService.getAllSongs();
        
        assertEquals(2, result.size());
    }

    @Test
    void updateSong_Success() {
        Song song = new Song();
        User admin = new User();
        admin.setRole(UserRole.ADMIN);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(admin));
        when(songRepository.save(any())).thenReturn(song);

        Boolean result = songService.updateSong(song);
        
        assertTrue(result);
        verify(songRepository).save(any(Song.class));
    }

    @Test
    void updateSong_NotAdmin() {
        Song song = new Song();
        User user = new User();
        user.setRole(UserRole.USER);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));

        assertThrows(TuneIslandException.class, () -> songService.updateSong(song));
    }

    @Test
    void searchSongsByName_Success() {
        List<Song> songs = Arrays.asList(new Song(), new Song());
        when(songRepository.findByNameContainingIgnoreCaseOrderByPlayAmountDesc("test"))
            .thenReturn(songs);

        List<Song> result = songService.searchSongsByName("test");
        
        assertEquals(2, result.size());
    }

    @Test
    void searchSongsByName_EmptyKeyword() {
        List<Song> result = songService.searchSongsByName("");
        assertTrue(result.isEmpty());
    }

    @Test
    void searchSongsBySinger_Success() {
        List<Song> songs = Arrays.asList(new Song(), new Song());
        when(songRepository.findBySingerContainingIgnoreCaseOrderByPlayAmountDesc("singer"))
            .thenReturn(songs);

        List<Song> result = songService.searchSongsBySinger("singer");
        
        assertEquals(2, result.size());
    }

    @Test
    void searchSongsBySinger_EmptyKeyword() {
        List<Song> result = songService.searchSongsBySinger("");
        assertTrue(result.isEmpty());
    }

    @Test
    void searchSongs_Success() {
        List<Song> songs = Arrays.asList(new Song(), new Song());
        when(songRepository.findByNameContainingIgnoreCaseOrSingerContainingIgnoreCaseOrderByPlayAmountDesc(
                anyString(), anyString())).thenReturn(songs);

        List<Song> result = songService.searchSongs("keyword");
        
        assertEquals(2, result.size());
    }

    @Test
    void searchSongs_EmptyKeyword() {
        List<Song> result = songService.searchSongs("");
        assertTrue(result.isEmpty());
    }

    @Test
    void rateSong_Success() {
        Song song = new Song();
        song.setSongId(1);
        song.setRate(4.0);
        song.setRateNum(1);
        song.setRateUserList(new ArrayList<>());
        User user = new User();
        user.setUserId(1);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songRepository.findBySongId(1)).thenReturn(song);
        when(songRepository.save(any())).thenReturn(song);

        Boolean result = songService.rateSong(1, 4.5);
        
        assertTrue(result);
        verify(songRepository).save(any(Song.class));
    }

    @Test
    void rateSong_AlreadyRated() {
        Song song = new Song();
        song.setSongId(1);
        song.setRate(4.0);
        song.setRateNum(1);
        List<Integer> rateUserList = new ArrayList<>();
        rateUserList.add(1);
        song.setRateUserList(rateUserList);
        User user = new User();
        user.setUserId(1);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songRepository.findBySongId(1)).thenReturn(song);

        assertThrows(TuneIslandException.class, () -> songService.rateSong(1, 4.5));
    }

    @Test
    void rateSong_InvalidRate() {
        Song song = new Song();
        song.setSongId(1);
        song.setRateUserList(new ArrayList<>());
        User user = new User();
        user.setUserId(1);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songRepository.findBySongId(1)).thenReturn(song);

        assertThrows(TuneIslandException.class, () -> songService.rateSong(1, 6.0));
    }

    @Test
    void play_Success() {
        Song song = new Song();
        song.setSongId(1);
        song.setPlayAmount(0);
        User user = new User();
        user.setUserId(1);
        user.setHistory(new ArrayList<>());
        
        when(songRepository.findBySongId(1)).thenReturn(song);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songRepository.save(any())).thenReturn(song);
        when(userFeign.save(any())).thenReturn(ResultVO.buildSuccess(user));

        Boolean result = songService.play(1);
        
        assertTrue(result);
        verify(songRepository).save(any(Song.class));
        verify(userFeign).save(any(User.class));
    }

    @Test
    void play_SongNotFound() {
        when(songRepository.findBySongId(1)).thenReturn(null);

        assertThrows(TuneIslandException.class, () -> songService.play(1));
    }

    @Test
    void collectSong_Success() {
        Song song = new Song();
        song.setSongId(1);
        Songlist songlist = new Songlist();
        songlist.setSongs(new ArrayList<>());
        songlist.setOwnerId(1);
        User user = new User();
        user.setUserId(1);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(songlistRepository.save(any())).thenReturn(songlist);

        Boolean result = songService.collectSong(1, 1);
        
        assertTrue(result);
        verify(songlistRepository).save(any(Songlist.class));
    }

    @Test
    void collectSong_AlreadyCollected() {
        Song song = new Song();
        song.setSongId(1);
        Songlist songlist = new Songlist();
        List<Integer> songs = new ArrayList<>();
        songs.add(1);
        songlist.setSongs(songs);
        songlist.setOwnerId(1);
        User user = new User();
        user.setUserId(1);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);

        assertThrows(TuneIslandException.class, () -> songService.collectSong(1, 1));
    }

    @Test
    void likeSong_Success() {
        Song song = new Song();
        song.setSongId(1);
        Songlist songlist = new Songlist();
        songlist.setSongs(new ArrayList<>());
        User user = new User();
        user.setUserId(1);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songlistService.getMylikeSonglist(1)).thenReturn(songlist);
        when(songlistRepository.save(any())).thenReturn(songlist);

        Boolean result = songService.likeSong(1);
        
        assertTrue(result);
        verify(songlistRepository).save(any(Songlist.class));
    }

    @Test
    void likeSong_AlreadyLiked() {
        Song song = new Song();
        song.setSongId(1);
        Songlist songlist = new Songlist();
        List<Integer> songs = new ArrayList<>();
        songs.add(1);
        songlist.setSongs(songs);
        User user = new User();
        user.setUserId(1);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songlistService.getMylikeSonglist(1)).thenReturn(songlist);

        assertThrows(TuneIslandException.class, () -> songService.likeSong(1));
    }

    @Test
    void cancelLikeSong_Success() {
        Song song = new Song();
        song.setSongId(1);
        Songlist songlist = new Songlist();
        List<Integer> songs = new ArrayList<>();
        songs.add(1);
        songlist.setSongs(songs);
        User user = new User();
        user.setUserId(1);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songlistService.getMylikeSonglist(1)).thenReturn(songlist);
        when(songlistRepository.save(any())).thenReturn(songlist);

        Boolean result = songService.cancelLikeSong(1);
        
        assertTrue(result);
        verify(songlistRepository).save(any(Songlist.class));
    }

    @Test
    void cancelLikeSong_NotLiked() {
        Song song = new Song();
        song.setSongId(1);
        Songlist songlist = new Songlist();
        songlist.setSongs(new ArrayList<>());
        User user = new User();
        user.setUserId(1);
        
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songlistService.getMylikeSonglist(1)).thenReturn(songlist);

        assertThrows(TuneIslandException.class, () -> songService.cancelLikeSong(1));
    }

    @Test
    void getRecommendedSongs_Success() {
        List<Integer> recommendedIds = Arrays.asList(1, 2);
        Song song1 = new Song();
        song1.setSongId(1);
        Song song2 = new Song();
        song2.setSongId(2);
        User user = new User();
        user.setUserId(1);
        user.setRecommendedSongs(recommendedIds);
        
        when(userFeign.getUserById(1)).thenReturn(ResultVO.buildSuccess(user));
        when(songRepository.findBySongId(1)).thenReturn(song1);
        when(songRepository.findBySongId(2)).thenReturn(song2);

        List<Song> result = songService.getRecommendedSongs(1, 10);
        
        assertEquals(2, result.size());
    }

    @Test
    void getRecommendedSongs_NoRecommendations() {
        User user = new User();
        user.setUserId(1);
        List<Song> songs = Arrays.asList(new Song(), new Song());
        
        when(userFeign.getUserById(1)).thenReturn(ResultVO.buildSuccess(user));
        when(songRepository.findAllByOrderByPlayAmountDesc()).thenReturn(songs);

        List<Song> result = songService.getRecommendedSongs(1, 10);
        
        assertEquals(2, result.size());
    }

    @Test
    void trainRecommendationModel_Success() {
        List<User> users = Arrays.asList(new User(), new User());
        List<Song> songs = Arrays.asList(new Song(), new Song());
        
        when(userFeign.findAll()).thenReturn(ResultVO.buildSuccess(users));
        when(songRepository.findAll()).thenReturn(songs);
        when(userFeign.saveAll(any())).thenReturn(ResultVO.buildSuccess(users));

        songService.trainRecommendationModel();
        
        verify(mlRecommenderUtil).trainModel(any(), any());
        verify(userFeign).saveAll(any());
    }

    @Test
    void getSongsByTag_Success() {
        List<Song> songs = Arrays.asList(new Song(), new Song());
        when(songRepository.findByTagsContainingOrderByPlayAmountDesc(SongTag.POP)).thenReturn(songs);

        List<Song> result = songService.getSongsByTag("POP");
        
        assertEquals(2, result.size());
    }

    @Test
    void getSongsByTag_EmptyTag() {
        List<Song> result = songService.getSongsByTag("");
        assertTrue(result.isEmpty());
    }

    @Test
    void getSongsByTag_NullTag() {
        List<Song> result = songService.getSongsByTag(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void getSongsByTag_InvalidTag() {
        List<Song> result = songService.getSongsByTag("INVALID_TAG");
        assertTrue(result.isEmpty());
    }

    @Test
    void getHotSongs_Success() {
        List<Song> songs = Arrays.asList(
            createSongWithPlayAmount(100),
            createSongWithPlayAmount(50)
        );
        when(songRepository.findAllByOrderByPlayAmountDesc()).thenReturn(songs);

        List<Song> result = songService.getHotSongs();
        
        assertEquals(2, result.size());
        assertTrue(result.get(0).getPlayAmount() >= result.get(1).getPlayAmount());
    }

    @Test
    void getHotSongs_EmptyList() {
        when(songRepository.findAllByOrderByPlayAmountDesc()).thenReturn(new ArrayList<>());

        List<Song> result = songService.getHotSongs();
        
        assertTrue(result.isEmpty());
    }

    private Song createSongWithPlayAmount(int playAmount) {
        Song song = new Song();
        song.setPlayAmount(playAmount);
        return song;
    }
} 