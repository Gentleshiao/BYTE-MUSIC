package com.devops26.comment.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.devops26.comment.entity.Comment;
import com.devops26.comment.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;

class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
        objectMapper = new ObjectMapper();

        testComment = new Comment();
        testComment.setCommentId(1);
        testComment.setUserId(1);
        testComment.setText("Test comment");
        testComment.setArtId(1);
        testComment.setIsSongComment(true);
        testComment.setLikes(0);
        testComment.setCreateTime(new Date());
    }

    @Test
    void createComment_Success() throws Exception {
        when(commentService.createComment(any(Comment.class))).thenReturn(1);

        mockMvc.perform(post("/comment/createComment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result").value(1));
    }

    @Test
    void deleteComment_Success() throws Exception {
        when(commentService.deleteComment(anyInt())).thenReturn(true);

        mockMvc.perform(post("/comment/deleteComment")
                .param("commentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result").value(true));
    }

    @Test
    void getSongCommentByArtId_Success() throws Exception {
        List<Comment> comments = Arrays.asList(testComment);
        when(commentService.getSongCommentByArtId(anyInt())).thenReturn(comments);

        mockMvc.perform(get("/comment/getSongCommentByArtId")
                .param("songId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result[0].commentId").value(testComment.getCommentId()));
    }

    @Test
    void getSonglistCommentByArtId_Success() throws Exception {
        List<Comment> comments = Arrays.asList(testComment);
        when(commentService.getSonglistCommentByArtId(anyInt())).thenReturn(comments);

        mockMvc.perform(get("/comment/getSonglistCommentByArtId")
                .param("songlistId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result[0].commentId").value(testComment.getCommentId()));
    }

    @Test
    void getByCommentId_Success() throws Exception {
        when(commentService.getByCommentId(anyInt())).thenReturn(testComment);

        mockMvc.perform(get("/comment/getByCommentId")
                .param("commentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result.commentId").value(testComment.getCommentId()));
    }

    @Test
    void likeComment_Success() throws Exception {
        when(commentService.likeComment(anyInt())).thenReturn(1);

        mockMvc.perform(post("/comment/likeComment")
                .param("commentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result").value(1));
    }

    @Test
    void cancelLikeComment_Success() throws Exception {
        when(commentService.cancelLikeComment(anyInt())).thenReturn(0);

        mockMvc.perform(post("/comment/cancelLikeComment")
                .param("commentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("000"))
                .andExpect(jsonPath("$.result").value(0));
    }
} 