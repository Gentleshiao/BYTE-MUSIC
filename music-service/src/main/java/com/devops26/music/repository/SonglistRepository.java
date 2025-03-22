package com.devops26.music.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.devops26.music.entity.Songlist;

public interface SonglistRepository extends JpaRepository<Songlist, Integer> {
    Songlist findBySonglistId(Integer songlistId);
    List<Songlist> findAllByOwnerId(Integer ownerId);
    Songlist findByName(String name);
    void deleteBySonglistId(Integer songlistId);
    List<Songlist> findAllByIsPublic(Boolean isPublic);
} 