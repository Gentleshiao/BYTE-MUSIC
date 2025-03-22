package com.devops26.music.repository;

import java.util.List;

import com.devops26.music.enums.SongTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.devops26.music.entity.Song;

public interface SongRepository extends JpaRepository<Song, Integer> {
    Song findBySongId(Integer songId);
    List<Song> findAll();
    Song findByUrl(String url);
    List<Song> findByNameContainingIgnoreCaseOrderByPlayAmountDesc(String name);
    List<Song> findBySingerContainingIgnoreCaseOrderByPlayAmountDesc(String singer);
    List<Song> findByNameContainingIgnoreCaseOrSingerContainingIgnoreCaseOrderByPlayAmountDesc(String name, String singer);

    List<Song> findByTagsContainingOrderByPlayAmountDesc(SongTag tag);

    List<Song> findAllByOrderByPlayAmountDesc();

    List<Song> findByRateIsNotNullOrderByRateDesc();
} 