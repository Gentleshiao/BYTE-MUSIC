package com.devops26.comment.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.devops26.comment.entity.User;
import com.devops26.comment.exception.TuneIslandException;
import com.devops26.comment.feign.UserFeign;
import com.devops26.comment.repository.CommentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devops26.comment.entity.Comment;
import com.devops26.comment.service.CommentService;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CommentServiceImpl implements CommentService {
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserFeign userFeign;

    @Override
    public Integer createComment(Comment comment) {
        try {
            comment.setCommentId(null);
            comment.setLikes(0);
            comment.setCreateTime(new Date());
            comment.setLikeUserList(new ArrayList<>());
            Comment savedComment = commentRepository.save(comment);
            log.info("Successfully created comment with ID: {}", savedComment.getCommentId());
            return savedComment.getCommentId();
        } catch (Exception e) {
            log.error("Error creating comment: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public Boolean deleteComment(Integer commentId) {
        User user = userFeign.getCurrentUser().getResult();
        Comment comment = commentRepository.findByCommentId(commentId);
        if (comment == null) {
            throw TuneIslandException.commentNotFound();
        }

        if (!Objects.equals(comment.getUserId(), user.getUserId())) {
            throw TuneIslandException.permissionDenied();
        }

        try {
            commentRepository.deleteByCommentId(commentId);
            log.info("Successfully deleted comment with ID: {}", commentId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting comment with ID {}: {}", commentId, e.getMessage());
            throw e;
        }
    }

    @Override
    public List<Comment> getSongCommentByArtId(Integer songId) {
        return commentRepository.findAllByArtIdAndAndIsSongComment(songId, true);
    }

    @Override
    public List<Comment> getSonglistCommentByArtId(Integer songlistId) {
        return commentRepository.findAllByArtIdAndAndIsSongComment(songlistId, false);
    }

    @Override
    public Comment getByCommentId(Integer commentId) {
        return commentRepository.findByCommentId(commentId);
    }

    @Override
    public Integer likeComment(Integer commentId) {
        Comment comment = commentRepository.findByCommentId(commentId);
        if (comment == null) {
            throw TuneIslandException.commentNotFound();
        }

        User user = userFeign.getCurrentUser().getResult();
        if (comment.getLikeUserList().contains(user.getUserId())) {
            throw TuneIslandException.alreadyLiked();
        }

        try {
            comment.setLikes(comment.getLikes() + 1);
            comment.getLikeUserList().add(user.getUserId());
            commentRepository.save(comment);
            log.info("User {} successfully liked comment {}", user.getUserId(), commentId);
            return comment.getLikes();
        } catch (Exception e) {
            log.error("Error while liking comment {}: {}", commentId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Integer cancelLikeComment(Integer commentId) {
        Comment comment = commentRepository.findByCommentId(commentId);
        if (comment == null) {
            throw TuneIslandException.commentNotFound();
        }

        User user = userFeign.getCurrentUser().getResult();
        if (!comment.getLikeUserList().contains(user.getUserId())) {
            throw new TuneIslandException("你还没有点赞过该评论");
        }

        try {
            comment.setLikes(comment.getLikes() - 1);
            comment.getLikeUserList().remove(user.getUserId());
            commentRepository.save(comment);
            log.info("User {} successfully cancelled like on comment {}", user.getUserId(), commentId);
            return comment.getLikes();
        } catch (Exception e) {
            log.error("Error while cancelling like on comment {}: {}", commentId, e.getMessage());
            throw e;
        }
    }
}
