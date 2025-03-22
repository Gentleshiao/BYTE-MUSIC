package com.devops26.music.entity;

import com.devops26.music.enums.SongTag;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "song_id")
    private Integer songId;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "singer")
    private String singer;

    @Basic
    @Column(name = "url")
    private String url;

    @Basic
    @Column(name = "play_amount")
    private Integer playAmount;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @Column(name = "tags")
    private List<SongTag> tags;

    @Basic
    @Column(name = "image_url")
    private String imageUrl;

    @Basic
    @Column(name = "rate")
    private Double rate;

    @Basic
    @Column(name = "rate_num")
    private Integer rateNum;

    @ElementCollection
    @Column(name = "rate_user_list")
    private List<Integer> rateUserList;

    @Basic
    @Column(name = "lyric", columnDefinition = "TEXT")
    private String lyric;

}
