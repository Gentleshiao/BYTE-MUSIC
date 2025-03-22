package com.devops26.music.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.devops26.music.constants.DefaultImage;
import com.devops26.music.entity.Songlist;
import com.devops26.music.entity.User;
import com.devops26.music.enums.SongTag;
import com.devops26.music.enums.UserRole;
import com.devops26.music.feign.UserFeign;
import com.devops26.music.repository.SonglistRepository;
import com.devops26.music.service.SonglistService;
import com.devops26.music.util.MLRecommenderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.devops26.music.entity.Song;
import com.devops26.music.exception.TuneIslandException;
import com.devops26.music.repository.SongRepository;
import com.devops26.music.service.SongService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SongServiceImpl implements SongService {
    @Autowired
    private UserFeign userFeign;

    @Autowired
    private SonglistRepository songlistRepository;

    @Autowired
    private SonglistService songlistService;

    @Autowired
    private MLRecommenderUtil mlRecommenderUtil;

    @Autowired
    private SongRepository songRepository;

    @Override
    public Integer uploadSong(Song song) {
        try {
            User user = userFeign.getCurrentUser().getResult();
            if (user.getRole() != UserRole.ADMIN) {
                throw TuneIslandException.permissionDenied();
            }
            if (songRepository.findByUrl(song.getUrl()) != null) {
                throw TuneIslandException.songAlreadyExists();
            }
            if (song.getImageUrl() == null || song.getImageUrl().isEmpty()) {
                song.setImageUrl(DefaultImage.DEFAULT_SONG_IMAGE);
            }
            Song newSong = songRepository.save(song);
            log.info("Successfully uploaded song: {}", newSong.getSongId());
            return newSong.getSongId();
        } catch (Exception e) {
            log.error("Failed to upload song: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Song getSongById(Integer songId) {
        return songRepository.findBySongId(songId);
    }

    @Override
    public List<Song> getAllSongs() {
        return songRepository.findAll();
    }

    @Override
    public Boolean updateSong(Song song) {
        User user = userFeign.getCurrentUser().getResult();
        if (user.getRole() != UserRole.ADMIN) {
            throw TuneIslandException.permissionDenied();
        }
        songRepository.save(song);
        return true;
    }

    @Override
    public List<Song> searchSongsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return List.of();
        }
        List<Song> songs = songRepository.findByNameContainingIgnoreCaseOrderByPlayAmountDesc(name.trim());
        return songs;
    }

    @Override
    public List<Song> searchSongsBySinger(String singer) {
        if (singer == null || singer.trim().isEmpty()) {
            return List.of();
        }
        List<Song> songs = songRepository.findBySingerContainingIgnoreCaseOrderByPlayAmountDesc(singer.trim());
        return songs;
    }


    @Override
    public List<Song> searchSongs(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }

        String trimmedKeyword = keyword.trim();
        List<Song> songs = songRepository.findByNameContainingIgnoreCaseOrSingerContainingIgnoreCaseOrderByPlayAmountDesc(
                trimmedKeyword, trimmedKeyword);

        return songs;
    }

    @Override
    public List<Song> getSongsByTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return List.of();
        }

        try {
            SongTag songTag = SongTag.valueOf(tag.toUpperCase());
            List<Song> songs = songRepository.findByTagsContainingOrderByPlayAmountDesc(songTag);
            return songs;
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    @Override
    public Boolean rateSong(Integer songId, Double rate) {
        try {
            User currentUser = userFeign.getCurrentUser().getResult();
            Song song = songRepository.findBySongId(songId);
            if (song.getRateUserList().contains(currentUser.getUserId())) {
                throw TuneIslandException.alreadyRated();
            }
            updateSongRating(song, rate, currentUser.getUserId());
            log.info("User {} rated song {} with {}", currentUser.getUserId(), songId, rate);
            return true;
        } catch (Exception e) {
            log.error("Error rating song {}: {}", songId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Cacheable(value = "rank", key = "'hot'")
    public List<Song> getHotSongs() {
        List<Song> songs = songRepository.findAllByOrderByPlayAmountDesc();
        songs.forEach(song -> song.setTags(null));
        songs.forEach(song -> song.setRateUserList(null));
        return songs.stream()
                .limit(30)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "rank", key = "#tag")
    public List<Song> getListByTag(String tag) {
        List<Song> songs = songRepository.findByTagsContainingOrderByPlayAmountDesc(SongTag.valueOf(tag));
        songs.forEach(song -> song.setTags(null));
        songs.forEach(song -> song.setRateUserList(null));
        return songs.stream().limit(30).collect(Collectors.toList());
    }



    @Transactional
    @Override
    public Boolean play(Integer songId) {
        try {
            synchronized (("songPlay:" + songId).intern()) {
                Song song = songRepository.findBySongId(songId);
                if (song == null) {
                    throw TuneIslandException.songNotFound();
                }

                if (song.getPlayAmount() == null) {
                    song.setPlayAmount(0);
                }
                song.setPlayAmount(song.getPlayAmount() + 1);
                songRepository.save(song);
            }

            User currentUser = userFeign.getCurrentUser().getResult();
            if (currentUser != null) {
                updateUserHistory(currentUser, songId);
            }
            return true;
        } catch (Exception e) {
            log.error("Error recording play for song {}: {}", songId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Boolean collectSong(Integer songId, Integer songlistId) {
        try {
            User user = userFeign.getCurrentUser().getResult();
            Songlist songlist = songlistRepository.findBySonglistId(songlistId);
            if (!songlist.getOwnerId().equals(user.getUserId())) {
                throw TuneIslandException.permissionDenied();
            }
            if (songlist.getSongs().contains(songId)) {
                throw TuneIslandException.songAlreadyCollected();
            }
            songlist.getSongs().add(songId);
            songlistRepository.save(songlist);
            log.info("User {} collected song {} to songlist {}", user.getUserId(), songId, songlistId);
            return true;
        } catch (Exception e) {
            log.error("Error collecting song {} to songlist {}: {}", songId, songlistId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Boolean likeSong(Integer songId) {
        try {
            User user = userFeign.getCurrentUser().getResult();
            Songlist likes = songlistService.getMylikeSonglist(user.getUserId());
            if (likes.getSongs().contains(songId)) {
                throw TuneIslandException.songAlreadyCollected();
            }
            likes.getSongs().add(songId);
            songlistRepository.save(likes);
            log.info("User {} liked song {}", user.getUserId(), songId);
            return true;
        } catch (Exception e) {
            log.error("Error liking song {}: {}", songId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Boolean cancelLikeSong(Integer songId) {
        try {
            User user = userFeign.getCurrentUser().getResult();
            Songlist likes = songlistService.getMylikeSonglist(user.getUserId());
            if (!likes.getSongs().contains(songId)) {
                throw new TuneIslandException("你还没有喜欢这首歌");
            }
            likes.getSongs().remove(songId);
            songlistRepository.save(likes);
            log.info("User {} cancelled like for song {}", user.getUserId(), songId);
            return true;
        } catch (Exception e) {
            log.error("Error cancelling like for song {}: {}", songId, e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void trainRecommendationModel() {
        List<User> users = userFeign.findAll().getResult();
        List<Song> songs = songRepository.findAll();
        mlRecommenderUtil.trainModel(users, songs);
        // 保存更新后的用户推荐列表
        userFeign.saveAll(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Song> getRecommendedSongs(Integer userId, Integer numRecommendations) {
        User user = userFeign.getUserById(userId).getResult();

        // 直接从用户的推荐列表中获取歌曲
        List<Integer> recommendedIds = user.getRecommendedSongs();
        if (recommendedIds == null || recommendedIds.isEmpty()) {
            // 如果没有推荐列表，返回热门歌曲
            return songRepository.findAllByOrderByPlayAmountDesc()
                    .stream()
                    .limit(numRecommendations)
                    .collect(Collectors.toList());
        }

        // 获取推荐的歌曲详情
        return recommendedIds.stream()
                .map(songRepository::findBySongId)
                .filter(song -> song != null)
                .limit(numRecommendations)
                .collect(Collectors.toList());
    }

    private void updateSongRating(Song song, Double rate, Integer userId) {
        if (rate < 0 || rate > 5) {
            throw TuneIslandException.invalidRate();
        }
        synchronized (("songRate:" + song.getSongId()).intern()) {
            Double totalRate = song.getRate() * song.getRateNum() + rate;
            song.setRateNum(song.getRateNum() + 1);
            song.setRate(totalRate / song.getRateNum());
            song.getRateUserList().add(userId);
            songRepository.save(song);
        }

    }

    private void updateUserHistory(User user, Integer songId) {
        if (user.getHistory() == null) {
            user.setHistory(new ArrayList<>());
        }
        user.getHistory().add(0, songId);
        if (user.getHistory().size() > 300) {
            user.getHistory().remove(300);
        }
        userFeign.save(user);
    }
}