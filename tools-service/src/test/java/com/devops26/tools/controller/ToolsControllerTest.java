package com.devops26.tools.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.devops26.tools.service.ToolsService;

class ToolsControllerTest {

    @Mock
    private ToolsService toolsService;

    @InjectMocks
    private ToolsController toolsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(toolsController).build();
    }

    @Test
    void upload_Success() throws Exception {
        // 准备测试数据
        String fileName = "test.jpg";
        String expectedUrl = "http://example.com/test.jpg";
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            fileName,
            MediaType.IMAGE_JPEG_VALUE,
            "test image content".getBytes()
        );

        // 模拟service层的行为
        when(toolsService.upload(any())).thenReturn(expectedUrl);

        // 执行测试并验证结果
        mockMvc.perform(multipart("/tools/upload")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(000));
    }

    @Test
    void extractMainColor_Success() throws Exception {
        // 准备测试数据
        String imageUrl = "http://example.com/test.jpg";
        String expectedColor = "java.awt.Color[r=255,g=0,b=0]";

        // 模拟service层的行为
        when(toolsService.extractMainColor(anyString())).thenReturn(expectedColor);

        // 执行测试并验证结果
        mockMvc.perform(get("/tools/extractMainColor")
                .param("imageUrl", imageUrl)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(000));
    }

    @Test
    void extractMainColor_WithInvalidUrl() throws Exception {
        // 准备测试数据
        String invalidImageUrl = "invalid-url";
        String defaultColor = "java.awt.Color[r=129,g=133,b=131]";

        // 模拟service层返回默认颜色
        when(toolsService.extractMainColor(anyString())).thenReturn(defaultColor);

        // 执行测试并验证结果
        mockMvc.perform(get("/tools/extractMainColor")
                .param("imageUrl", invalidImageUrl)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(000));
    }
} 