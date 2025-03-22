package com.devops26.music.util;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.devops26.music.entity.Song;
import com.devops26.music.entity.User;
import com.devops26.music.enums.SongTag;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.springframework.stereotype.Component;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MLModelTrainer {
    private static final int LATENT_FACTORS = 50;
    private static final double LEARNING_RATE = 0.005;
    private static final double REGULARIZATION = 0.015;
    private static final int MAX_ITERATIONS = 1000;
    private static final int PATIENCE = 10;
    
    private RealMatrix userFactors;
    private RealMatrix songFactors;
    private RealMatrix tagFactors;
    
    public void trainModel(RealMatrix ratingMatrix, List<User> users, List<Song> songs) {
        initializeFactorMatrices(users.size(), songs.size(), SongTag.values().length);
        trainWithSGD(ratingMatrix);
    }
    
    private void initializeFactorMatrices(int numUsers, int numSongs, int numTags) {
        Random random = new Random(new Date().getTime());
        userFactors = initializeMatrix(numUsers, LATENT_FACTORS, random);
        songFactors = initializeMatrix(numSongs, LATENT_FACTORS, random);
        tagFactors = initializeMatrix(numTags, LATENT_FACTORS, random);
    }
    
    private void trainWithSGD(RealMatrix ratingMatrix) {
        ModelState bestState = new ModelState(Double.MAX_VALUE);
        int noImprovement = 0;
        
        for (int iter = 0; iter < MAX_ITERATIONS && noImprovement < PATIENCE; iter++) {
            TrainingResult result = performOneIteration(ratingMatrix);
            
            if (result.rmse < bestState.rmse) {
                bestState.update(result.rmse, userFactors, songFactors, tagFactors);
                noImprovement = 0;
            } else {
                noImprovement++;
            }
            
            logProgress(iter, result.rmse);
        }
        
        restoreBestState(bestState);
    }
    
    private TrainingResult performOneIteration(RealMatrix ratingMatrix) {
        List<int[]> trainingPairs = getTrainingPairs(ratingMatrix);
        Collections.shuffle(trainingPairs);
        
        double totalError = 0.0;
        int numRatings = trainingPairs.size();
        
        for (int[] pair : trainingPairs) {
            double error = updateFactorsForPair(pair, ratingMatrix);
            totalError += error * error;
        }
        
        return new TrainingResult(Math.sqrt(totalError / numRatings));
    }
    
    private double updateFactorsForPair(int[] pair, RealMatrix ratingMatrix) {
        double actualRating = ratingMatrix.getEntry(pair[0], pair[1]);
        double predictedRating = predict(pair[0], pair[1]);
        double error = actualRating - predictedRating;
        
        updateFactors(pair[0], pair[1], error);
        return error;
    }
    
    public double predict(int userId, int songId) {
        try {
            double prediction = 0.0;
            for (int f = 0; f < LATENT_FACTORS; f++) {
                prediction += userFactors.getEntry(userId, f) * songFactors.getEntry(songId, f);
            }
            return prediction;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private void updateFactors(int userId, int songId, double error) {
        for (int f = 0; f < LATENT_FACTORS; f++) {
            double userFactor = userFactors.getEntry(userId, f);
            double songFactor = songFactors.getEntry(songId, f);
            
            userFactors.setEntry(userId, f, userFactor + LEARNING_RATE * 
                (error * songFactor - REGULARIZATION * userFactor));
            songFactors.setEntry(songId, f, songFactor + LEARNING_RATE * 
                (error * userFactor - REGULARIZATION * songFactor));
        }
    }
    
    private List<int[]> getTrainingPairs(RealMatrix ratingMatrix) {
        return IntStream.range(0, ratingMatrix.getRowDimension())
            .boxed()
            .flatMap(u -> IntStream.range(0, ratingMatrix.getColumnDimension())
                .filter(s -> ratingMatrix.getEntry(u, s) > 0)
                .mapToObj(s -> new int[]{u, s}))
            .collect(Collectors.toList());
    }
    
    private RealMatrix initializeMatrix(int rows, int cols, Random random) {
        RealMatrix matrix = MatrixUtils.createRealMatrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix.setEntry(i, j, random.nextDouble() * 0.1);
            }
        }
        return matrix;
    }
    
    private void logProgress(int iteration, double rmse) {
        if ((iteration + 1) % 10 == 0) {
            log.info("Iteration {}: RMSE = {}", iteration + 1, rmse);
        }
    }
    
    private void restoreBestState(ModelState bestState) {
        if (bestState.hasValidState()) {
            userFactors = bestState.userFactors;
            songFactors = bestState.songFactors;
            tagFactors = bestState.tagFactors;
        }
    }
    
    @Data
    @AllArgsConstructor
    private static class ModelState {
        private double rmse;
        private RealMatrix userFactors;
        private RealMatrix songFactors;
        private RealMatrix tagFactors;
        
        public ModelState(double rmse) {
            this.rmse = rmse;
        }
        
        public void update(double rmse, RealMatrix userFactors, 
                RealMatrix songFactors, RealMatrix tagFactors) {
            this.rmse = rmse;
            this.userFactors = userFactors.copy();
            this.songFactors = songFactors.copy();
            this.tagFactors = tagFactors.copy();
        }
        
        public boolean hasValidState() {
            return userFactors != null && songFactors != null && tagFactors != null;
        }
    }
    
    @Value
    private static class TrainingResult {
        double rmse;
    }
} 