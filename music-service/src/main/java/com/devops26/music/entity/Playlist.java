package com.devops26.music.entity;

import com.devops26.music.enums.PlayStrategy;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "playlist")
public class Playlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "playlist_id")
    private Integer playlistId;

    @Basic
    @Column(name = "user_id")
    private Integer userId;

    @Basic
    @Column(name = "current_song")
    private Integer currentSong;

    @Basic
    @Column(name = "current_progress")
    private Integer currentProgress; // 当前进度

    @Basic
    @Column(name = "play_strategy")
    @Enumerated(EnumType.STRING)
    private PlayStrategy playStrategy;

    @ElementCollection
    @Column(name = "songs")
    private List<Integer> songs;
}