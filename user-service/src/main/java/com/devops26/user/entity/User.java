package com.devops26.user.entity;

import com.devops26.user.enums.UserRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "password")
    private String password;

    @Basic
    @Column(name = "phone")
    private String phone;

    @Basic
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Basic
    @Column(name = "image_url")
    private String imageUrl;

    // 收藏的歌单
    @ElementCollection
    @Column(name = "songlist_list")
    private List<Integer> songlistList;

    @ElementCollection
    @Column(name = "history")
    private List<Integer> history;

    @ElementCollection
    @Column(name = "recommended_songs")
    private List<Integer> recommendedSongs;

}