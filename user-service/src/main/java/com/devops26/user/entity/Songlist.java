package com.devops26.user.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Songlist {
    private Integer songlistId;

    private String name;

    private Integer ownerId;

    private Integer collects;

    private String imageUrl;

    private Boolean isPublic;

    private List<Integer> songs;

    private Double rate;

    private Integer rateNum;

    private List<Integer> rateUserList;
}
