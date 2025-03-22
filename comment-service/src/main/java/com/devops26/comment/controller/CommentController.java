package com.devops26.comment.controller;

import java.util.List;

import com.devops26.comment.entity.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devops26.comment.entity.Comment;
import com.devops26.comment.service.CommentService;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    CommentService commentService;

    @PostMapping("/createComment")
    public ResultVO<Integer> createComment(@RequestBody Comment comment) {
        return ResultVO.buildSuccess(commentService.createComment(comment));
    }

    @PostMapping("/deleteComment")
    public ResultVO<Boolean> deleteComment(Integer commentId) {
        return ResultVO.buildSuccess(commentService.deleteComment(commentId));
    }

    @GetMapping("/getSongCommentByArtId")
    public ResultVO<List<Comment>> getSongCommentByArtId(Integer songId) {
        return ResultVO.buildSuccess(commentService.getSongCommentByArtId(songId));
    }

    @GetMapping("/getSonglistCommentByArtId")
    public ResultVO<List<Comment>> getSonglistCommentByArtId(Integer songlistId) {
        return ResultVO.buildSuccess(commentService.getSonglistCommentByArtId(songlistId));
    }

    @GetMapping("/getByCommentId")
    public ResultVO<Comment> getByCommentId(Integer commentId) {
        return ResultVO.buildSuccess(commentService.getByCommentId(commentId));
    }

    @PostMapping("/likeComment")
    public ResultVO<Integer> likeComment(Integer commentId) {
        return ResultVO.buildSuccess(commentService.likeComment(commentId));
    }

    @PostMapping("/cancelLikeComment")
    public ResultVO<Integer> cancelLikeComment(Integer commentId) {
        return ResultVO.buildSuccess(commentService.cancelLikeComment(commentId));
    }
}