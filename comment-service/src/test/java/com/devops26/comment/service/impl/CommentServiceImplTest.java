package com.devops26.comment.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.devops26.comment.entity.Comment;
import com.devops26.comment.entity.ResultVO;
import com.devops26.comment.entity.User;
import com.devops26.comment.exception.TuneIslandException;
import com.devops26.comment.feign.UserFeign;
import com.devops26.comment.repository.CommentRepository;

class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserFeign userFeign;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment testComment;
    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setUserId(1);

        testComment = new Comment();
        testComment.setCommentId(1);
        testComment.setUserId(1);
        testComment.setText("Test comment");
        testComment.setArtId(1);
        testComment.setIsSongComment(true);
        testComment.setLikes(0);
        testComment.setCreateTime(new Date());
        testComment.setLikeUserList(new ArrayList<>());
    }

    @Test
    void createComment_Success() throws IOException {
        Comment savedComment = new Comment();
        savedComment.setCommentId(1);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        Integer commentId = commentService.createComment(testComment);

        assertNotNull(commentId);
        assertEquals(savedComment.getCommentId(), commentId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_Failure() {
        when(commentRepository.save(any(Comment.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> commentService.createComment(testComment));
    }

    @Test
    void deleteComment_Success() {
        ResultVO<User> userResultVO = new ResultVO<>();
        userResultVO.setResult(testUser);
        when(userFeign.getCurrentUser()).thenReturn(userResultVO);
        when(commentRepository.findByCommentId(testComment.getCommentId())).thenReturn(testComment);
        doNothing().when(commentRepository).deleteByCommentId(testComment.getCommentId());

        Boolean result = commentService.deleteComment(testComment.getCommentId());

        assertTrue(result);
        verify(commentRepository).deleteByCommentId(testComment.getCommentId());
    }

    @Test
    void deleteComment_CommentNotFound() {
        ResultVO<User> userResultVO = new ResultVO<>();
        userResultVO.setResult(testUser);
        when(userFeign.getCurrentUser()).thenReturn(userResultVO);
        when(commentRepository.findByCommentId(anyInt())).thenReturn(null);

        assertThrows(TuneIslandException.class, () -> commentService.deleteComment(999));
    }

    @Test
    void deleteComment_PermissionDenied() {
        ResultVO<User> userResultVO = new ResultVO<>();
        User differentUser = new User();
        differentUser.setUserId(2);
        userResultVO.setResult(differentUser);
        when(userFeign.getCurrentUser()).thenReturn(userResultVO);
        when(commentRepository.findByCommentId(testComment.getCommentId())).thenReturn(testComment);

        assertThrows(TuneIslandException.class, () -> commentService.deleteComment(testComment.getCommentId()));
    }

    @Test
    void getSongCommentByArtId_Success() {
        List<Comment> expectedComments = Arrays.asList(testComment);
        when(commentRepository.findAllByArtIdAndAndIsSongComment(anyInt(), eq(true)))
                .thenReturn(expectedComments);

        List<Comment> actualComments = commentService.getSongCommentByArtId(1);

        assertEquals(expectedComments.size(), actualComments.size());
        assertEquals(expectedComments.get(0).getCommentId(), actualComments.get(0).getCommentId());
    }

    @Test
    void getSonglistCommentByArtId_Success() {
        List<Comment> expectedComments = Arrays.asList(testComment);
        when(commentRepository.findAllByArtIdAndAndIsSongComment(anyInt(), eq(false)))
                .thenReturn(expectedComments);

        List<Comment> actualComments = commentService.getSonglistCommentByArtId(1);

        assertEquals(expectedComments.size(), actualComments.size());
        assertEquals(expectedComments.get(0).getCommentId(), actualComments.get(0).getCommentId());
    }

    @Test
    void getByCommentId_Success() {
        when(commentRepository.findByCommentId(testComment.getCommentId())).thenReturn(testComment);

        Comment result = commentService.getByCommentId(testComment.getCommentId());

        assertNotNull(result);
        assertEquals(testComment.getCommentId(), result.getCommentId());
    }

    @Test
    void likeComment_Success() {
        ResultVO<User> userResultVO = new ResultVO<>();
        userResultVO.setResult(testUser);
        when(userFeign.getCurrentUser()).thenReturn(userResultVO);
        when(commentRepository.findByCommentId(testComment.getCommentId())).thenReturn(testComment);
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Integer likes = commentService.likeComment(testComment.getCommentId());

        assertEquals(1, likes);
        assertTrue(testComment.getLikeUserList().contains(testUser.getUserId()));
    }

    @Test
    void likeComment_AlreadyLiked() {
        testComment.getLikeUserList().add(testUser.getUserId());
        ResultVO<User> userResultVO = new ResultVO<>();
        userResultVO.setResult(testUser);
        when(userFeign.getCurrentUser()).thenReturn(userResultVO);
        when(commentRepository.findByCommentId(testComment.getCommentId())).thenReturn(testComment);

        assertThrows(TuneIslandException.class, () -> commentService.likeComment(testComment.getCommentId()));
    }

    @Test
    void cancelLikeComment_Success() {
        testComment.getLikeUserList().add(testUser.getUserId());
        testComment.setLikes(1);
        ResultVO<User> userResultVO = new ResultVO<>();
        userResultVO.setResult(testUser);
        when(userFeign.getCurrentUser()).thenReturn(userResultVO);
        when(commentRepository.findByCommentId(testComment.getCommentId())).thenReturn(testComment);
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);

        Integer likes = commentService.cancelLikeComment(testComment.getCommentId());

        assertEquals(0, likes);
        assertFalse(testComment.getLikeUserList().contains(testUser.getUserId()));
    }

    @Test
    void cancelLikeComment_NotLiked() {
        ResultVO<User> userResultVO = new ResultVO<>();
        userResultVO.setResult(testUser);
        when(userFeign.getCurrentUser()).thenReturn(userResultVO);
        when(commentRepository.findByCommentId(testComment.getCommentId())).thenReturn(testComment);

        assertThrows(TuneIslandException.class, () -> commentService.cancelLikeComment(testComment.getCommentId()));
    }

    @Test
    void createComment_WithNullProperties() {
        Comment comment = new Comment();  // 所有属性都为null
        Comment savedComment = new Comment();
        savedComment.setCommentId(1);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        Integer commentId = commentService.createComment(comment);

        assertNotNull(commentId);
        assertEquals(savedComment.getCommentId(), commentId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void deleteComment_UserFeignReturnsNull() {
        when(userFeign.getCurrentUser()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> commentService.deleteComment(testComment.getCommentId()));
    }

    @Test
    void deleteComment_UserFeignThrowsException() {
        when(userFeign.getCurrentUser()).thenThrow(new RuntimeException("Feign client error"));

        assertThrows(RuntimeException.class, () -> commentService.deleteComment(testComment.getCommentId()));
    }

    @Test
    void likeComment_UserFeignReturnsNull() {
        when(commentRepository.findByCommentId(testComment.getCommentId())).thenReturn(testComment);
        when(userFeign.getCurrentUser()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> commentService.likeComment(testComment.getCommentId()));
    }

    @Test
    void likeComment_UserFeignThrowsException() {
        when(commentRepository.findByCommentId(testComment.getCommentId())).thenReturn(testComment);
        when(userFeign.getCurrentUser()).thenThrow(new RuntimeException("Feign client error"));

        assertThrows(RuntimeException.class, () -> commentService.likeComment(testComment.getCommentId()));
    }

    @Test
    void likeComment_SaveThrowsDataIntegrityException() {
        ResultVO<User> userResultVO = new ResultVO<>();
        userResultVO.setResult(testUser);
        when(userFeign.getCurrentUser()).thenReturn(userResultVO);
        when(commentRepository.findByCommentId(testComment.getCommentId())).thenReturn(testComment);
        when(commentRepository.save(any(Comment.class))).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Data integrity error"));

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> commentService.likeComment(testComment.getCommentId()));
    }

    @Test
    void cancelLikeComment_UserFeignReturnsNull() {
        when(commentRepository.findByCommentId(testComment.getCommentId())).thenReturn(testComment);
        when(userFeign.getCurrentUser()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> commentService.cancelLikeComment(testComment.getCommentId()));
    }

    @Test
    void cancelLikeComment_UserFeignThrowsException() {
        when(commentRepository.findByCommentId(testComment.getCommentId())).thenReturn(testComment);
        when(userFeign.getCurrentUser()).thenThrow(new RuntimeException("Feign client error"));

        assertThrows(RuntimeException.class, () -> commentService.cancelLikeComment(testComment.getCommentId()));
    }

    @Test
    void cancelLikeComment_SaveThrowsDataIntegrityException() {
        testComment.getLikeUserList().add(testUser.getUserId());
        ResultVO<User> userResultVO = new ResultVO<>();
        userResultVO.setResult(testUser);
        when(userFeign.getCurrentUser()).thenReturn(userResultVO);
        when(commentRepository.findByCommentId(testComment.getCommentId())).thenReturn(testComment);
        when(commentRepository.save(any(Comment.class))).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Data integrity error"));

        assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () -> commentService.cancelLikeComment(testComment.getCommentId()));
    }

    @Test
    void getSongCommentByArtId_ThrowsException() {
        when(commentRepository.findAllByArtIdAndAndIsSongComment(anyInt(), eq(true)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> commentService.getSongCommentByArtId(1));
    }

    @Test
    void getSonglistCommentByArtId_ThrowsException() {
        when(commentRepository.findAllByArtIdAndAndIsSongComment(anyInt(), eq(false)))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> commentService.getSonglistCommentByArtId(1));
    }

    @Test
    void getByCommentId_ThrowsException() {
        when(commentRepository.findByCommentId(anyInt()))
                .thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> commentService.getByCommentId(1));
    }
} 