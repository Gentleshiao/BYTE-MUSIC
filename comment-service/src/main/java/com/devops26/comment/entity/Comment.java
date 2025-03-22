package com.devops26.comment.entity;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Integer commentId;

    @Basic
    @Column(name = "text")
    private String text;

    @Basic
    @Column(name = "likes")
    private Integer likes;

    @Basic
    @Column(name = "parent_id")
    private Integer parentId;

    @ElementCollection
    @Column(name = "like_user_list")
    private List<Integer> likeUserList;

    @Basic
    @Column(name = "is_song_comment")
    private Boolean isSongComment;

    @Basic
    @Column(name = "art_id")
    private Integer artId;

    @Basic
    @Column(name = "user_id")
    private Integer userId;

    @Basic
    @Column(name = "create_time")
    private Date createTime;
}