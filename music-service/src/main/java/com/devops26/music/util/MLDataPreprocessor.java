package com.devops26.music.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.devops26.music.entity.Song;
import com.devops26.music.entity.User;
import com.devops26.music.repository.SonglistRepository;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MLDataPreprocessor {
    @Autowired
    private SonglistRepository songlistRepository;
    
    public RealMatrix buildRatingMatrix(List<User> users, List<Song> songs) {
        RealMatrix matrix = MatrixUtils.createRealMatrix(users.size(), songs.size());
        Map<Integer, Integer> songIdToIndex = createSongIdToIndexMap(songs);
        
        for (int userIndex = 0; userIndex < users.size(); userIndex++) {
            processUserIfHasHistory(matrix, new UserRatingContext(
                userIndex, users.get(userIndex), songs, songIdToIndex));
        }
        
        return matrix;
    }
    
    private Map<Integer, Integer> createSongIdToIndexMap(List<Song> songs) {
        Map<Integer, Integer> songIdToIndex = new HashMap<>();
        for (int i = 0; i < songs.size(); i++) {
            songIdToIndex.put(songs.get(i).getSongId(), i);
        }
        return songIdToIndex;
    }
    
    private void processUserIfHasHistory(RealMatrix matrix, UserRatingContext context) {
        if (context.getUser().getHistory() != null) {
            processUserRatings(matrix, context);
        }
    }
    
    public void processUserRatings(RealMatrix matrix, UserRatingContext context) {
        Map<Integer, Integer> playCounts = calculatePlayCounts(context.getUser());
        int maxPlays = getMaxPlayCount(playCounts);
        
        playCounts.forEach((songId, playCount) -> 
            Optional.ofNullable(context.getSongIdToIndex().get(songId))
                .ifPresent(songIndex -> 
                    setMatrixEntry(matrix, context, songIndex, playCount, maxPlays)));
    }
    
    private void setMatrixEntry(RealMatrix matrix, UserRatingContext context, 
            int songIndex, int playCount, int maxPlays) {
        Song song = context.getSongs().get(songIndex);
        double rating = calculateRating(context.getUser(), song, playCount, maxPlays);
        matrix.setEntry(context.getUserIndex(), songIndex, rating);
    }
    
    public Map<Integer, Integer> calculatePlayCounts(User user) {
        Map<Integer, Integer> songPlayCounts = new HashMap<>();
        if (user.getHistory() != null) {
            user.getHistory().forEach(songId -> 
                songPlayCounts.merge(songId, 1, Integer::sum));
        }
        return songPlayCounts;
    }
    
    private int getMaxPlayCount(Map<Integer, Integer> songPlayCounts) {
        return songPlayCounts.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(1);
    }
    
    private double calculateRating(User user, Song song, int playCount, int maxPlays) {
        double baseScore = (double) playCount / maxPlays;
        double likeBonus = isUserLikedSong(user, song.getSongId()) ? 0.3 : 0;
        double rateBonus = calculateRateBonus(user, song);
        
        return Math.min(baseScore + likeBonus + rateBonus, 1.0);
    }
    
    private boolean isUserLikedSong(User user, Integer songId) {
        return Optional.ofNullable(user.getSonglistList())
            .map(lists -> lists.stream()
                .map(songlistRepository::findBySonglistId)
                .filter(songlist -> songlist != null && songlist.getSongs() != null)
                .anyMatch(songlist -> songlist.getSongs().contains(songId)))
            .orElse(false);
    }
    
    private double calculateRateBonus(User user, Song song) {
        return Optional.ofNullable(song.getRateUserList())
            .filter(list -> list.contains(user.getUserId()))
            .map(list -> (song.getRate() / 5.0) * 0.2)
            .orElse(0.0);
    }
    
    @Value
    public static class UserRatingContext {
        int userIndex;
        User user;
        List<Song> songs;
        Map<Integer, Integer> songIdToIndex;
    }
} 