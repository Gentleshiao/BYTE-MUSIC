package com.devops26.music.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.devops26.music.constants.DefaultImage;
import com.devops26.music.entity.User;
import com.devops26.music.feign.UserFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.devops26.music.entity.Song;
import com.devops26.music.entity.Songlist;
import com.devops26.music.exception.TuneIslandException;
import com.devops26.music.repository.SongRepository;
import com.devops26.music.repository.SonglistRepository;
import com.devops26.music.service.SonglistService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SonglistServiceImpl implements SonglistService {

    @Autowired
    SonglistRepository songlistRepository;

    @Autowired
    UserFeign userFeign;

    @Override
    public Integer createSonglist(Songlist songlist) {
        try {
            if (songlistRepository.findByName(songlist.getName()) != null) {
                throw TuneIslandException.songlistAlreadyExists();
            }
            if (songlist.getImageUrl() == null || songlist.getImageUrl().isEmpty()) {
                songlist.setImageUrl(DefaultImage.DEFAULT_SONGLIST_IMAGE);
            }
            Songlist saved = songlistRepository.save(songlist);
            log.info("Successfully created songlist: {}", saved.getSonglistId());
            return saved.getSonglistId();
        } catch (Exception e) {
            log.error("Failed to create songlist: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public Boolean deleteSonglist(Integer songlistId) {
        try {
            Songlist songlist = songlistRepository.findBySonglistId(songlistId);
            if (!Objects.equals(userFeign.getCurrentUser().getResult().getUserId(), songlist.getOwnerId())) {
                throw TuneIslandException.permissionDenied();
            }
            songlistRepository.deleteBySonglistId(songlistId);
            log.info("Successfully deleted songlist: {}", songlistId);
            return true;
        } catch (Exception e) {
            log.error("Failed to delete songlist {}: {}", songlistId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Boolean updateSonglist(Songlist songlist) {
        try {
            Songlist oldSonglist = songlistRepository.findBySonglistId(songlist.getSonglistId());
            if (!Objects.equals(userFeign.getCurrentUser().getResult().getUserId(), oldSonglist.getOwnerId())) {
                throw TuneIslandException.permissionDenied();
            }
            songlistRepository.save(songlist);
            log.info("Successfully updated songlist: {}", songlist.getSonglistId());
            return true;
        } catch (Exception e) {
            log.error("Failed to update songlist {}: {}", songlist.getSonglistId(), e.getMessage());
            throw e;
        }
    }

    @Override
    public Songlist getMylikeSonglist(Integer userId) {
        User user = userFeign.getUserById(userId).getResult();
        String mylikeName = user.getName() + "喜欢的音乐";
        return songlistRepository.findByName(mylikeName);
    }

    @Override
    public List<Songlist> getAllByOwnerId(Integer ownerId) {
        return songlistRepository.findAllByOwnerId(ownerId);
    }

    @Override
    public Songlist getBySonglistId(Integer songlistId) {
        return songlistRepository.findBySonglistId(songlistId);
    }

    @Override
    public Songlist getByName(String name) {
        return songlistRepository.findByName(name);
    }

    @Override
    public Integer collectSonglist(Integer songlistId) {
        try {
            User user = userFeign.getCurrentUser().getResult();
            if (user.getSonglistList().contains(songlistId)) {
                throw new TuneIslandException("你已经收藏过这首歌了");
            }

            user.getSonglistList().add(songlistId);
            userFeign.save(user);
            synchronized (("songlistCollect:" + songlistId).intern()) {
                Songlist songlist = songlistRepository.findBySonglistId(songlistId);
                songlist.setCollects(songlist.getCollects() + 1);
                songlistRepository.save(songlist);
                log.info("User {} collected songlist {}", user.getUserId(), songlistId);
                return songlist.getCollects();
            }
        } catch (Exception e) {
            log.error("Error collecting songlist {}: {}", songlistId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Double rate(Integer songlistId, Double rate) {
        try {
            if (rate < 0 || rate > 5) {
                throw TuneIslandException.invalidRate();
            }

            User currentUser = userFeign.getCurrentUser().getResult();
            Songlist songlist = songlistRepository.findBySonglistId(songlistId);

            if (songlist == null) {
                throw TuneIslandException.songlistNotFound();
            }

            initRate(songlist);

            if (songlist.getRateUserList().contains(currentUser.getUserId())) {
                throw TuneIslandException.alreadyRated();
            }

            synchronized (("songlistRate:" + songlistId).intern()) {
                songlist = songlistRepository.findBySonglistId(songlistId);
                Double totalRate = songlist.getRate() * songlist.getRateNum() + rate;
                songlist.setRateNum(songlist.getRateNum() + 1);
                songlist.setRate(totalRate / songlist.getRateNum());
                songlist.getRateUserList().add(currentUser.getUserId());
                Double newRate = songlistRepository.save(songlist).getRate();
                log.info("User {} rated songlist {} with {}", currentUser.getUserId(), songlistId, rate);
                return newRate;
            }
        } catch (Exception e) {
            log.error("Error rating songlist {}: {}", songlistId, e.getMessage());
            throw e;
        }
    }

    private void initRate(Songlist songlist) {
        if (songlist.getRate() == null) {
            songlist.setRate(0.0);
        }
        if (songlist.getRateNum() == null) {
            songlist.setRateNum(0);
        }
        if (songlist.getRateUserList() == null) {
            songlist.setRateUserList(new ArrayList<>());
        }
    }

    @Override
    public List<Songlist> getPublicSonglists() {
        return songlistRepository.findAllByIsPublic(true);
    }

    @Override
    public Boolean cancelCollectSonglist(Integer songlistId) {
        try {
            User user = userFeign.getCurrentUser().getResult();
            if (!user.getSonglistList().contains(songlistId)) {
                throw new TuneIslandException("你还没有收藏过这个歌单");
            }
            Songlist songlist = songlistRepository.findBySonglistId(songlistId);
            user.getSonglistList().remove(songlistId);
            userFeign.save(user);
            songlist.setCollects(songlist.getCollects() - 1);
            songlistRepository.save(songlist);
            log.info("User {} cancelled collection of songlist {}", user.getUserId(), songlistId);
            return true;
        } catch (Exception e) {
            log.error("Error cancelling collection of songlist {}: {}", songlistId, e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Songlist> getRecommendedSonglists() {
        // 获取所有公开歌单
        List<Songlist> publicSonglists = songlistRepository.findAllByIsPublic(true);
        for (Songlist songlist : publicSonglists) {
            if (songlist.getRate() == null) songlist.setRate(0.0);
        }
        return publicSonglists.stream()
                .sorted((s1, s2) -> {
                    // 首先按评分排序（从高到低）
                    int rateCompare = s2.getRate().compareTo(s1.getRate());
                    if (rateCompare != 0) {
                        return rateCompare;
                    }
                    // 评分相同时，按收藏数排序（从高到低）
                    return s2.getCollects().compareTo(s1.getCollects());
                })
                .limit(8)
                .collect(Collectors.toList());
    }

    public boolean createDefaultSonglist(User user) {
        Songlist songlist = new Songlist();
        songlist.setOwnerId(user.getUserId());
        songlist.setName(user.getName() + "喜欢的音乐");
        songlist.setImageUrl(DefaultImage.DEFAULT_MYLIKE_IMAGE);
        songlist.setCollects(0);
        songlist.setIsPublic(false);
        songlist.setSongs(new ArrayList<>());
        songlist.setRate(0.0);
        songlist.setRateNum(0);
        songlist.setRateUserList(new ArrayList<>());
        createSonglist(songlist);
        log.info("Created default songlist for user: {}", user.getUserId());
        return true;
    }

    public boolean updateMyLikeSonglistName(User user) {
        Songlist songlist = getMylikeSonglist(user.getUserId());
        songlist.setName(user.getName() + "喜欢的音乐");
        songlistRepository.save(songlist);
        return true;
    }
}
