package com.devops26.music.entity;

import com.devops26.music.enums.UserRole;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class User {
    private Integer userId;

    private String name;

    private String password;

    private String phone;

    private UserRole role;

    private String imageUrl;

    // 收藏的歌单
    private List<Integer> songlistList;

    private List<Integer> history;

    private List<Integer> recommendedSongs;

}
