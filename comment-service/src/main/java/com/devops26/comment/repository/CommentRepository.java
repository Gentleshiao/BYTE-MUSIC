package com.devops26.comment.repository;


import com.devops26.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    Comment findByCommentId(Integer commentId);

    List<Comment> findAllByArtIdAndAndIsSongComment(Integer artId, boolean isSongComment);

    void deleteByCommentId(Integer commentId);

}
