package com.devops26.comment.service;

import java.util.List;

import com.devops26.comment.entity.Comment;

public interface CommentService {
    Integer createComment(Comment comment);

    Boolean deleteComment(Integer commentId);

    List<Comment> getSongCommentByArtId(Integer songId);

    List<Comment> getSonglistCommentByArtId(Integer artId);

    Comment getByCommentId(Integer commentId);

    Integer likeComment(Integer commentId);

    Integer cancelLikeComment(Integer commentId);
} 