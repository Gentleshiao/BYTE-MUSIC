package com.devops26.music.config;

import com.devops26.music.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ScheduleConfig {

    @Autowired
    private SongService songService;

    @Scheduled(cron = "0 0 4 * * ?") // 每天凌晨4点执行
    @CacheEvict(value = "rank")
    public void trainRecommendationModel() {
        songService.trainRecommendationModel();
    }
}

