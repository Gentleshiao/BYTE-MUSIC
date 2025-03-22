package com.devops26.music.util;

import java.util.List;
import java.util.stream.Collectors;

import com.devops26.music.entity.Song;
import com.devops26.music.entity.User;
import org.apache.commons.math3.linear.RealMatrix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MLRecommenderUtil {
    private static final int DEFAULT_RECOMMENDATIONS = 10;
    
    @Autowired
    private MLModelTrainer modelTrainer;
    @Autowired
    private MLDataPreprocessor dataPreprocessor;
    @Autowired
    private MLRecommendationEngine recommendationEngine;
    
    public void trainModel(List<User> users, List<Song> songs) {
        if (users.isEmpty() || songs.isEmpty()) {
            return;
        }
        
        try {
            RealMatrix ratingMatrix = dataPreprocessor.buildRatingMatrix(users, songs);
            modelTrainer.trainModel(ratingMatrix, users, songs);
            
            for (User user : users) {
                try {
                    List<Song> recommendations = recommendationEngine
                        .recommendSongsForUser(user, songs, DEFAULT_RECOMMENDATIONS);
                    user.setRecommendedSongs(
                        recommendations.stream()
                            .map(Song::getSongId)
                            .collect(Collectors.toList())
                    );
                } catch (Exception e) {
                    log.error("Error generating recommendations for user {}: {}", 
                            user.getUserId(), e.getMessage());
                    user.setRecommendedSongs(null);
                }
            }
        } catch (Exception e) {
            log.error("Error during model training: {}", e.getMessage());
            throw e;
        }
    }
} 