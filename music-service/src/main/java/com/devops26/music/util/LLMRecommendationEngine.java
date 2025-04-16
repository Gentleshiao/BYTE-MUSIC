package com.devops26.music.util;

import com.devops26.music.entity.Song;
import com.devops26.music.entity.User;
import com.devops26.music.enums.SongTag;
import com.devops26.music.repository.SongRepository;
import com.devops26.music.repository.SonglistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LLMRecommendationEngine {
    private static final int DEFAULT_RECOMMENDATIONS = 10;
    
    @Value("${llm.api.url}")
    private String llmApiUrl;
    
    @Value("${llm.api.key}")
    private String llmApiKey;
    
    @Autowired
    private SongRepository songRepository;
    
    @Autowired
    private SonglistRepository songlistRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public List<Song> recommendSongsForUser(User user, List<Song> allSongs, int numRecommendations) {
        try {
            String prompt = buildRecommendationPrompt(user, allSongs, numRecommendations);
            List<Integer> recommendedSongIds = callLLMAPI(prompt, numRecommendations);
            
            return recommendedSongIds.stream()
                .map(songRepository::findBySongId)
                .filter(Objects::nonNull)
                .limit(numRecommendations)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error generating LLM recommendations for user {}: {}", 
                    user.getUserId(), e.getMessage());
            return getFallbackRecommendations(allSongs, numRecommendations);
        }
    }
    
    private String buildRecommendationPrompt(User user, List<Song> allSongs, int numRecommendations) {
        StringBuilder prompt = new StringBuilder();
        
        // 添加用户基本信息
        prompt.append("基于以下用户信息和歌曲库，请推荐").append(numRecommendations).append("首歌曲：\n\n");
        
        // 添加用户播放历史
        if (user.getHistory() != null && !user.getHistory().isEmpty()) {
            prompt.append("用户最近播放的歌曲：\n");
            user.getHistory().stream()
                .limit(10)
                .map(songId -> songRepository.findBySongId(songId))
                .filter(Objects::nonNull)
                .forEach(song -> prompt.append("- ").append(song.getName())
                    .append(" (").append(song.getSinger()).append(")\n"));
        }
        
        // 添加用户喜欢的歌曲
        Set<Integer> likedSongs = getUserLikedSongs(user);
        if (!likedSongs.isEmpty()) {
            prompt.append("\n用户喜欢的歌曲：\n");
            likedSongs.stream()
                .limit(10)
                .map(songRepository::findBySongId)
                .filter(Objects::nonNull)
                .forEach(song -> prompt.append("- ").append(song.getName())
                    .append(" (").append(song.getSinger()).append(")\n"));
        }
        
        // 添加用户评分信息
        prompt.append("\n用户评分过的歌曲：\n");
        allSongs.stream()
            .filter(song -> song.getRateUserList() != null && 
                    song.getRateUserList().contains(user.getUserId()))
            .limit(10)
            .forEach(song -> prompt.append("- ").append(song.getName())
                .append(" (").append(song.getSinger()).append(") - 评分：")
                .append(song.getRate()).append("\n"));
        
        // 添加歌曲库信息
        prompt.append("\n可推荐的歌曲库：\n");
        allSongs.stream()
            .limit(50)
            .forEach(song -> {
                prompt.append("- ").append(song.getName())
                    .append(" (").append(song.getSinger()).append(")")
                    .append(" 标签：").append(getTagsString(song.getTags()))
                    .append(" 播放量：").append(song.getPlayAmount())
                    .append(" 评分：").append(song.getRate())
                    .append("\n");
            });
        
        prompt.append("\n请根据用户的听歌历史、喜好和评分，从歌曲库中选择").append(numRecommendations)
            .append("首最合适的歌曲推荐给用户。返回格式为歌曲ID列表，用逗号分隔。");
        
        return prompt.toString();
    }
    
    private List<Integer> callLLMAPI(String prompt, int numRecommendations) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + llmApiKey);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", 100);
        requestBody.put("temperature", 0.7);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(
                llmApiUrl, request, Map.class);
            
            String content = (String) response.get("choices");
            return Arrays.stream(content.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .limit(numRecommendations)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error calling LLM API: {}", e.getMessage());
            throw e;
        }
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
    
    private String getTagsString(List<SongTag> tags) {
        if (tags == null || tags.isEmpty()) {
            return "无";
        }
        return tags.stream()
            .map(SongTag::name)
            .collect(Collectors.joining(", "));
    }
    
    private List<Song> getFallbackRecommendations(List<Song> allSongs, int numRecommendations) {
        return allSongs.stream()
            .sorted((s1, s2) -> {
                // 首先按评分排序
                int rateCompare = s2.getRate().compareTo(s1.getRate());
                if (rateCompare != 0) {
                    return rateCompare;
                }
                // 评分相同时按播放量排序
                return s2.getPlayAmount().compareTo(s1.getPlayAmount());
            })
            .limit(numRecommendations)
            .collect(Collectors.toList());
    }
} 