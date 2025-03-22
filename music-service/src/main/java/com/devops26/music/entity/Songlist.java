package com.devops26.music.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Songlist {
    @Id
    @Column(name = "songlist_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer songlistId;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "owner_id")
    private Integer ownerId;

    @Basic
    @Column(name = "collects")
    private Integer collects;

    @Basic
    @Column(name = "image_url")
    private String imageUrl;

    @Basic
    @Column(name = "is_public")
    private Boolean isPublic;

    @ElementCollection
    @Column(name = "songs")
    private List<Integer> songs;

    @Basic
    @Column(name = "rate")
    private Double rate;

    @Basic
    @Column(name = "rate_num")
    private Integer rateNum;

    @ElementCollection
    @Column(name = "rate_user_list")
    private List<Integer> rateUserList;
}
