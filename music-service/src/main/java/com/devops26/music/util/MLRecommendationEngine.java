package com.devops26.music.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.devops26.music.entity.Song;
import com.devops26.music.entity.User;
import com.devops26.music.enums.SongTag;
import com.devops26.music.repository.SongRepository;
import com.devops26.music.repository.SonglistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MLRecommendationEngine {
    private static final int DEFAULT_RECOMMENDATIONS = 10;
    private static final double ML_WEIGHT = 0.60;
    private static final double TAG_WEIGHT = 0.20;
    private static final double LIKE_WEIGHT = 0.09;
    private static final double POPULARITY_WEIGHT = 0.10;
    private static final double RATING_WEIGHT = 0.01;
    
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private SonglistRepository songlistRepository;
    @Autowired
    private MLModelTrainer modelTrainer;
    
    public List<Song> recommendSongsForUser(User user, List<Song> allSongs, int numRecommendations) {
        try {
            RecommendationContext context = createRecommendationContext(user, allSongs);
            return generateRecommendations(context, numRecommendations);
        } catch (Exception e) {
            log.error("Error generating recommendations for user {}: {}", 
                    user.getUserId(), e.getMessage());
            return getPopularSongs(allSongs, numRecommendations);
        }
    }
    
    private RecommendationContext createRecommendationContext(User user, List<Song> allSongs) {
        return new RecommendationContext(
            user,
            allSongs,
            calculateUserTagPreferences(user),
            getUserLikedSongs(user)
        );
    }
    
    private List<Song> generateRecommendations(RecommendationContext context, int numRecommendations) {
        Map<Song, Double> songScores = calculateSongScores(context);
        return selectTopSongs(songScores, numRecommendations);
    }
    
    private Map<Song, Double> calculateSongScores(RecommendationContext context) {
        return context.getAllSongs().stream()
            .collect(Collectors.toMap(
                song -> song,
                song -> calculateComprehensiveScore(context, song)
            ));
    }
    
    private List<Song> selectTopSongs(Map<Song, Double> songScores, int numRecommendations) {
        return songScores.entrySet().stream()
            .sorted(Map.Entry.<Song, Double>comparingByValue().reversed())
            .limit(numRecommendations)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    private double calculateComprehensiveScore(RecommendationContext context, Song song) {
        return ML_WEIGHT * getMLScore(context.getUser(), song) +
               TAG_WEIGHT * calculateTagMatchingScore(song, context.getTagPreferences()) +
               LIKE_WEIGHT * getLikeScore(song, context.getLikedSongs()) +
               POPULARITY_WEIGHT * calculatePopularityScore(song) +
               RATING_WEIGHT * calculateRatingScore(song);
    }
    
    private double getMLScore(User user, Song song) {
        try {
            return modelTrainer.predict(user.getUserId(), song.getSongId());
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private double getLikeScore(Song song, Set<Integer> likedSongs) {
        return likedSongs.contains(song.getSongId()) ? 1.0 : 0.0;
    }
    
    private Map<SongTag, Double> calculateUserTagPreferences(User user) {
        Map<SongTag, Integer> tagCounts = new HashMap<>();
        int[] totalSongs = {0}; // 使用数组来在lambda中修改
        
        Optional.ofNullable(user.getHistory())
            .ifPresent(history -> history.forEach(songId -> 
                Optional.ofNullable(songRepository.findBySongId(songId))
                    .filter(song -> song.getTags() != null)
                    .ifPresent(song -> {
                        song.getTags().forEach(tag -> 
                            tagCounts.merge(tag, 1, Integer::sum));
                        totalSongs[0]++;
                    })));
        
        return calculateTagPreferences(tagCounts, totalSongs[0]);
    }
    
    private Map<SongTag, Double> calculateTagPreferences(Map<SongTag, Integer> tagCounts, int totalSongs) {
        if (totalSongs == 0) {
            return new HashMap<>();
        }
        
        Map<SongTag, Double> preferences = new HashMap<>();
        tagCounts.forEach((tag, count) -> 
            preferences.put(tag, (double) count / totalSongs));
        return preferences;
    }
    
    private double calculateTagMatchingScore(Song song, Map<SongTag, Double> tagPreferences) {
        if (song.getTags() == null || song.getTags().isEmpty() || tagPreferences.isEmpty()) {
            return 0.0;
        }
        
        return song.getTags().stream()
            .mapToDouble(tag -> tagPreferences.getOrDefault(tag, 0.0))
            .average()
            .orElse(0.0);
    }
    
    private Set<Integer> getUserLikedSongs(User user) {
        return Optional.ofNullable(user.getSonglistList())
            .map(lists -> lists.stream()
                .map(songlistRepository::findBySonglistId)
                .filter(songlist -> songlist != null && songlist.getSongs() != null)
                .flatMap(songlist -> songlist.getSongs().stream())
                .collect(Collectors.toSet()))
            .orElse(Set.of());
    }
    
    private double calculatePopularityScore(Song song) {
        return Optional.ofNullable(song.getPlayAmount())
            .map(amount -> {
                int maxPlayAmount = songRepository.findAllByOrderByPlayAmountDesc()
                    .stream()
                    .findFirst()
                    .map(Song::getPlayAmount)
                    .orElse(1);
                return (double) amount / maxPlayAmount;
            })
            .orElse(0.0);
    }
    
    private double calculateRatingScore(Song song) {
        return Optional.of(song)
            .filter(s -> s.getRate() != null && s.getRateNum() != null && s.getRateNum() > 0)
            .map(s -> (s.getRate() / 5.0) * Math.min(1.0, s.getRateNum() / 10.0))
            .orElse(0.0);
    }
    
    private List<Song> getPopularSongs(List<Song> allSongs, int numRecommendations) {
        return allSongs.stream()
            .sorted((s1, s2) -> s2.getPlayAmount().compareTo(s1.getPlayAmount()))
            .limit(numRecommendations)
            .collect(Collectors.toList());
    }
    
    @Value
    private static class RecommendationContext {
        User user;
        List<Song> allSongs;
        Map<SongTag, Double> tagPreferences;
        Set<Integer> likedSongs;
    }
} 