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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.devops26.music.constants.DefaultImage;
import com.devops26.music.entity.ResultVO;
import com.devops26.music.entity.Songlist;
import com.devops26.music.entity.User;
import com.devops26.music.exception.TuneIslandException;
import com.devops26.music.feign.UserFeign;
import com.devops26.music.repository.SonglistRepository;

class SonglistServiceImplTest {

    @Mock
    private SonglistRepository songlistRepository;

    @Mock
    private UserFeign userFeign;

    @InjectMocks
    private SonglistServiceImpl songlistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createSonglist_Success() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setOwnerId(1);
        songlist.setName("Test Songlist");
        
        when(songlistRepository.findByName(anyString())).thenReturn(null);
        when(songlistRepository.save(any(Songlist.class))).thenReturn(songlist);

        Integer result = songlistService.createSonglist(songlist);
        
        assertNotNull(result);
        assertEquals(1, result);
        verify(songlistRepository).save(any(Songlist.class));
    }

    @Test
    void createSonglist_WithDefaultImage() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setOwnerId(1);
        songlist.setName("Test Songlist");
        songlist.setImageUrl("");
        
        when(songlistRepository.findByName(anyString())).thenReturn(null);
        when(songlistRepository.save(any(Songlist.class))).thenReturn(songlist);

        songlistService.createSonglist(songlist);
        
        verify(songlistRepository).save(argThat(s -> 
            s.getImageUrl().equals(DefaultImage.DEFAULT_SONGLIST_IMAGE)
        ));
    }

    @Test
    void createSonglist_AlreadyExists() {
        Songlist songlist = new Songlist();
        songlist.setOwnerId(1);
        songlist.setName("Test Songlist");
        
        when(songlistRepository.findByName(anyString())).thenReturn(songlist);

        assertThrows(TuneIslandException.class, () -> songlistService.createSonglist(songlist));
    }

    @Test
    void createSonglist_SaveError() {
        Songlist songlist = new Songlist();
        songlist.setName("Test Songlist");
        
        when(songlistRepository.findByName(anyString())).thenReturn(null);
        when(songlistRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class, () -> songlistService.createSonglist(songlist));
    }

    @Test
    void deleteSonglist_Success() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setOwnerId(1);
        songlist.setIsPublic(false);
        
        User user = new User();
        user.setUserId(1);
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));

        Boolean result = songlistService.deleteSonglist(1);
        
        assertTrue(result);
        verify(songlistRepository).deleteBySonglistId(1);
    }

    @Test
    void deleteSonglist_NotFound() {
        when(songlistRepository.findBySonglistId(1)).thenReturn(null);

        assertThrows(Exception.class, () -> songlistService.deleteSonglist(1));
    }

    @Test
    void deleteSonglist_NotOwner() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setOwnerId(1);
        songlist.setIsPublic(false);
        
        User user = new User();
        user.setUserId(2);
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));

        assertThrows(TuneIslandException.class, () -> songlistService.deleteSonglist(1));
    }

    @Test
    void deleteSonglist_DeleteError() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setOwnerId(1);
        
        User user = new User();
        user.setUserId(1);
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        doThrow(new RuntimeException("DB Error")).when(songlistRepository).deleteBySonglistId(1);

        assertThrows(RuntimeException.class, () -> songlistService.deleteSonglist(1));
    }

    @Test
    void updateSonglist_Success() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setOwnerId(1);
        songlist.setName("Updated Songlist");
        songlist.setIsPublic(false);
        
        User user = new User();
        user.setUserId(1);
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songlistRepository.save(any(Songlist.class))).thenReturn(songlist);

        Boolean result = songlistService.updateSonglist(songlist);
        
        assertTrue(result);
        verify(songlistRepository).save(songlist);
    }

    @Test
    void updateSonglist_NotFound() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(null);

        assertThrows(Exception.class, () -> songlistService.updateSonglist(songlist));
    }

    @Test
    void updateSonglist_NotOwner() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setOwnerId(1);
        
        User user = new User();
        user.setUserId(2);
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));

        assertThrows(TuneIslandException.class, () -> songlistService.updateSonglist(songlist));
    }

    @Test
    void updateSonglist_SaveError() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setOwnerId(1);
        
        User user = new User();
        user.setUserId(1);
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songlistRepository.save(any())).thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class, () -> songlistService.updateSonglist(songlist));
    }

    @Test
    void getMylikeSonglist_Success() {
        User user = new User();
        user.setUserId(1);
        user.setName("test");
        
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setOwnerId(1);
        songlist.setIsPublic(false);
        
        when(userFeign.getUserById(1)).thenReturn(ResultVO.buildSuccess(user));
        when(songlistRepository.findByName(user.getName() + "喜欢的音乐")).thenReturn(songlist);

        Songlist result = songlistService.getMylikeSonglist(1);
        
        assertNotNull(result);
        assertEquals(1, result.getSonglistId());
    }

    @Test
    void getMylikeSonglist_UserNotFound() {
        when(userFeign.getUserById(1)).thenReturn(ResultVO.buildFailure("User not found"));

        assertThrows(Exception.class, () -> songlistService.getMylikeSonglist(1));
    }


    @Test
    void getAllByOwnerId_Success() {
        List<Songlist> songlists = Arrays.asList(
            new Songlist(), new Songlist()
        );
        
        when(songlistRepository.findAllByOwnerId(1)).thenReturn(songlists);

        List<Songlist> result = songlistService.getAllByOwnerId(1);
        
        assertEquals(2, result.size());
    }

    @Test
    void getAllByOwnerId_Empty() {
        when(songlistRepository.findAllByOwnerId(1)).thenReturn(new ArrayList<>());

        List<Songlist> result = songlistService.getAllByOwnerId(1);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void getBySonglistId_Success() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);

        Songlist result = songlistService.getBySonglistId(1);
        
        assertNotNull(result);
        assertEquals(1, result.getSonglistId());
    }


    @Test
    void getByName_Success() {
        Songlist songlist = new Songlist();
        songlist.setName("test");
        
        when(songlistRepository.findByName("test")).thenReturn(songlist);

        Songlist result = songlistService.getByName("test");
        
        assertNotNull(result);
        assertEquals("test", result.getName());
    }


    @Test
    void collectSonglist_Success() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setCollects(0);
        
        User user = new User();
        user.setUserId(1);
        user.setSonglistList(new ArrayList<>());
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songlistRepository.save(any())).thenReturn(songlist);
        when(userFeign.save(any())).thenReturn(ResultVO.buildSuccess(user));

        Integer result = songlistService.collectSonglist(1);
        
        assertNotNull(result);
        verify(songlistRepository).save(any());
        verify(userFeign).save(any());
    }

    @Test
    void collectSonglist_AlreadyCollected() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        
        User user = new User();
        user.setUserId(1);
        List<Integer> songlistList = new ArrayList<>();
        songlistList.add(1);
        user.setSonglistList(songlistList);
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));

        assertThrows(TuneIslandException.class, () -> songlistService.collectSonglist(1));
    }

    @Test
    void rate_Success() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setRate(4.0);
        songlist.setRateNum(1);
        songlist.setRateUserList(new ArrayList<>());
        
        User user = new User();
        user.setUserId(1);
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songlistRepository.save(any())).thenReturn(songlist);

        Double result = songlistService.rate(1, 4.5);
        
        assertNotNull(result);
        verify(songlistRepository).save(any());
    }

    @Test
    void rate_InvalidRate() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setRateUserList(new ArrayList<>());
        
        User user = new User();
        user.setUserId(1);
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));

        assertThrows(TuneIslandException.class, () -> songlistService.rate(1, 6.0));
    }

    @Test
    void rate_AlreadyRated() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        List<Integer> rateUserList = new ArrayList<>();
        rateUserList.add(1);
        songlist.setRateUserList(rateUserList);
        
        User user = new User();
        user.setUserId(1);
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));

        assertThrows(TuneIslandException.class, () -> songlistService.rate(1, 4.5));
    }

    @Test
    void getPublicSonglists_Success() {
        List<Songlist> songlists = Arrays.asList(
            createSonglistWithRateAndCollects(4.5, 10),
            createSonglistWithRateAndCollects(4.0, 8)
        );
        
        when(songlistRepository.findAllByIsPublic(true)).thenReturn(songlists);

        List<Songlist> result = songlistService.getPublicSonglists();
        
        assertEquals(2, result.size());
        assertTrue(result.get(0).getRate() >= result.get(1).getRate());
    }

    @Test
    void getPublicSonglists_Empty() {
        when(songlistRepository.findAllByIsPublic(true)).thenReturn(new ArrayList<>());

        List<Songlist> result = songlistService.getPublicSonglists();
        
        assertTrue(result.isEmpty());
    }

    @Test
    void getRecommendedSonglists_Success() {
        List<Songlist> songlists = Arrays.asList(
            createSonglistWithRateAndCollects(4.5, 10),
            createSonglistWithRateAndCollects(4.0, 8)
        );
        
        when(songlistRepository.findAllByIsPublic(true)).thenReturn(songlists);
        when(userFeign.getUserById(anyInt())).thenReturn(ResultVO.buildSuccess(new User()));

        List<Songlist> result = songlistService.getRecommendedSonglists();
        
        assertEquals(2, result.size());
        assertTrue(result.get(0).getRate() >= result.get(1).getRate());
    }

    @Test
    void createDefaultSonglist_Success() {
        User user = new User();
        user.setUserId(1);
        user.setName("test");
        
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        
        when(songlistRepository.save(any())).thenReturn(songlist);

        boolean result = songlistService.createDefaultSonglist(user);
        
        assertTrue(result);
        verify(songlistRepository).save(argThat(s -> 
            s.getName().equals(user.getName() + "喜欢的音乐") &&
            s.getImageUrl().equals(DefaultImage.DEFAULT_MYLIKE_IMAGE)
        ));
    }

    @Test
    void updateMyLikeSonglistName_Success() {
        User user = new User();
        user.setUserId(1);
        user.setName("test");
        
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        
        when(songlistRepository.findByName(anyString())).thenReturn(songlist);
        when(songlistRepository.save(any())).thenReturn(songlist);
        when(userFeign.getUserById(1)).thenReturn(ResultVO.buildSuccess(user));

        boolean result = songlistService.updateMyLikeSonglistName(user);
        
        assertTrue(result);
        verify(songlistRepository).save(any());
    }

    @Test
    void getRecommendedSonglists_WithDifferentRatesAndCollects() {
        List<Songlist> songlists = Arrays.asList(
            createSonglistWithRateAndCollects(4.5, 10),  // Score: 4.5 * 0.7 + 10 * 0.3 = 6.15
            createSonglistWithRateAndCollects(4.0, 15),  // Score: 4.0 * 0.7 + 15 * 0.3 = 7.3
            createSonglistWithRateAndCollects(3.5, 5)    // Score: 3.5 * 0.7 + 5 * 0.3 = 3.95
        );
        
        when(songlistRepository.findAllByIsPublic(true)).thenReturn(songlists);
        when(userFeign.getUserById(anyInt())).thenReturn(ResultVO.buildSuccess(new User()));

        List<Songlist> result = songlistService.getRecommendedSonglists();
        
        assertEquals(3, result.size());
        // Verify that the list is sorted by rate
        assertTrue(result.get(0).getRate() >= result.get(1).getRate());
        assertTrue(result.get(1).getRate() >= result.get(2).getRate());
    }

    @Test
    void cancelCollectSonglist_Success() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setCollects(1);
        
        User user = new User();
        user.setUserId(1);
        List<Integer> songlistList = new ArrayList<>();
        songlistList.add(1);
        user.setSonglistList(songlistList);
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));
        when(songlistRepository.save(any())).thenReturn(songlist);
        when(userFeign.save(any())).thenReturn(ResultVO.buildSuccess(user));

        Boolean result = songlistService.cancelCollectSonglist(1);
        
        assertTrue(result);
        verify(songlistRepository).save(any(Songlist.class));
        verify(userFeign).save(any(User.class));
    }

    @Test
    void cancelCollectSonglist_NotCollected() {
        Songlist songlist = new Songlist();
        songlist.setSonglistId(1);
        songlist.setCollects(0);
        
        User user = new User();
        user.setUserId(1);
        user.setSonglistList(new ArrayList<>());
        
        when(songlistRepository.findBySonglistId(1)).thenReturn(songlist);
        when(userFeign.getCurrentUser()).thenReturn(ResultVO.buildSuccess(user));

        assertThrows(TuneIslandException.class, () -> songlistService.cancelCollectSonglist(1));
    }

    private Songlist createSonglistWithRateAndCollects(double rate, int collects) {
        Songlist songlist = new Songlist();
        songlist.setRate(rate);
        songlist.setCollects(collects);
        songlist.setIsPublic(true);
        return songlist;
    }
} 