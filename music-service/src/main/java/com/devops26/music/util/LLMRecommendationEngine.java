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
            log.info("========== Recommendation Process Begin ==========");
            log.info("Sending prompt to LLM: \n{}", prompt);
            
            List<Integer> recommendedSongIds = callLLMAPI(prompt, numRecommendations);
            log.info("Parsed song IDs from LLM response: {}", recommendedSongIds);
            
            List<Song> recommendedSongs = recommendedSongIds.stream()
                .map(songRepository::findBySongId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
                
            log.info("Final recommended songs count: {}", recommendedSongs.size());
            log.info("========== Recommendation Process End ============");
            
            return recommendedSongs;
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
                    .append("(").append(song.getSinger()).append(")\n"));
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
                    .append("(").append(song.getSinger()).append(")\n"));
        }
        
        // 添加用户评分信息
        prompt.append("\n用户评分过的歌曲：\n");
        allSongs.stream()
            .filter(song -> song.getRateUserList() != null && 
                    song.getRateUserList().contains(user.getUserId()))
            .limit(10)
            .forEach(song -> prompt.append("- ").append(song.getName())
                .append("(").append(song.getSinger()).append(") - 评分：")
                .append(song.getRate()).append("\n"));
        
        // 添加歌曲库信息
        prompt.append("\n可推荐的歌曲库：\n");
        allSongs.stream()
            .limit(50)
            .forEach(song -> {
                prompt.append("- 歌曲id: ").append(song.getSongId())
                    .append("；歌名(歌手): ").append(song.getName())
                    .append("(").append(song.getSinger()).append(")")
                    .append("；标签：").append(getTagsString(song.getTags()))
                    .append("；播放量：").append(song.getPlayAmount())
                    .append("；评分：").append(song.getRate())
                    .append("\n");
            });
        
        prompt.append("\n请根据用户的听歌历史、喜好和评分，从歌曲库中选择歌曲，按照推荐顺序将歌曲库中的歌曲id推荐给用户。")
              .append("如果可用歌曲数量不足").append(numRecommendations).append("首，")
              .append("请仅返回可用的歌曲ID即可。\n")
              .append("严格按照以下格式返回：推荐歌曲ID1,推荐歌曲ID2,推荐歌曲ID3...\n")
              .append("示例格式：15,2,4,1");
        
        return prompt.toString();
    }
    
    private List<Integer> callLLMAPI(String prompt, int numRecommendations) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + llmApiKey);
        
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messages", messages);
        requestBody.put("model", "deepseek-chat");
        requestBody.put("max_tokens", 100);
        requestBody.put("temperature", 0.7);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        try {
            Map<String, Object> response = restTemplate.postForObject(
                llmApiUrl, request, Map.class);
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("No response from LLM API");
            }

            Map<String, Object> choice = choices.get(0);
            Map<String, String> message = (Map<String, String>) choice.get("message");
            String content = message.get("content");

            log.info("========== LLM API Response Begin ==========");
            log.info("Raw response content:");
            log.info("{}", content);
            log.info("========== LLM API Response End ============");

            List<Integer> result = new ArrayList<>();
            
            String[] parts = content.split("[,，]");
            
            for (String part : parts) {
                try {
                    String cleaned = part.trim().replaceAll("[^0-9]", "");
                    if (!cleaned.isEmpty() && cleaned.length() <= 8) {
                        int id = Integer.parseInt(cleaned);
                        if (id > 0) {
                            result.add(id);
                        }
                    }
                } catch (NumberFormatException e) {
                    log.debug("Skipping invalid number: {}", part);
                }
            }
            
            if (result.isEmpty()) {
                List<String> numbers = new ArrayList<>();
                StringBuilder current = new StringBuilder();
                
                for (char c : content.toCharArray()) {
                    if (Character.isDigit(c)) {
                        current.append(c);
                    } else if (current.length() > 0) {
                        if (current.length() <= 8) {
                            numbers.add(current.toString());
                        }
                        current = new StringBuilder();
                    }
                }
                if (current.length() > 0 && current.length() <= 8) {
                    numbers.add(current.toString());
                }
                
                result = numbers.stream()
                    .map(Integer::parseInt)
                    .filter(id -> id > 0)
                    .collect(Collectors.toList());
            }

            if (result.isEmpty()) {
                throw new RuntimeException("No valid song IDs found in LLM response");
            }

            return result;

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