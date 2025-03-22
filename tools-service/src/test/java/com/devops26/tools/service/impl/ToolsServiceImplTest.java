package com.devops26.tools.service.impl;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.devops26.tools.exception.TuneIslandException;
import com.devops26.tools.util.ObsUtil;

class ToolsServiceImplTest {

    @Mock
    private ObsUtil obsUtil;

    @InjectMocks
    private ToolsServiceImpl toolsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void upload_Success() throws IOException {
        // 准备测试数据
        String fileName = "test.jpg";
        String expectedUrl = "http://example.com/test.jpg";
        MultipartFile file = new MockMultipartFile(fileName, fileName, "image/jpeg", "test data".getBytes());

        // 模拟ObsUtil的行为
        when(obsUtil.upload(anyString(), any(InputStream.class))).thenReturn(expectedUrl);

        // 执行测试
        String actualUrl = toolsService.upload(file);

        // 验证结果
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void upload_Failure() throws IOException {
        // 准备测试数据
        String fileName = "test.jpg";
        MultipartFile file = new MockMultipartFile(fileName, fileName, "image/jpeg", "test data".getBytes());

        // 模拟ObsUtil抛出异常
        when(obsUtil.upload(anyString(), any(InputStream.class))).thenThrow(RuntimeException.class);

        // 验证是否抛出预期的异常
        assertThrows(TuneIslandException.class, () -> toolsService.upload(file));
    }

    @Test
    void extractMainColor_Success() throws IOException {
        // 创建一个测试用的图片
        BufferedImage testImage = createTestImage(Color.RED, false);
        
        // 创建一个可以被测试方法使用的ToolsServiceImpl子类
        ToolsServiceImpl testService = new ToolsServiceImpl() {
            @Override
            public BufferedImage loadImage(String imageUrl) {
                return testImage;
            }
        };

        // 执行测试
        String colorResult = testService.extractMainColor("http://example.com/test.jpg");

        // 验证结果不为空
        assertNotNull(colorResult);
        assertTrue(colorResult.startsWith("java.awt.Color"));
    }

    @Test
    void extractMainColor_WithDeepColor() throws IOException {
        // 创建一个深色测试图片
        BufferedImage testImage = createTestImage(new Color(50, 50, 50), true);
        
        ToolsServiceImpl testService = new ToolsServiceImpl() {
            @Override
            public BufferedImage loadImage(String imageUrl) {
                return testImage;
            }
        };

        String colorResult = testService.extractMainColor("http://example.com/test.jpg");
        
        assertNotNull(colorResult);
        assertTrue(colorResult.contains("r=50,g=50,b=50"));
    }

    @Test
    void extractMainColor_WithLightColor() throws IOException {
        // 创建一个浅色测试图片
        BufferedImage testImage = createTestImage(new Color(200, 200, 200), false);
        
        ToolsServiceImpl testService = new ToolsServiceImpl() {
            @Override
            public BufferedImage loadImage(String imageUrl) {
                return testImage;
            }
        };

        String colorResult = testService.extractMainColor("http://example.com/test.jpg");
        
        assertNotNull(colorResult);
        // 由于是浅色图片，且没有深色像素，应该返回默认颜色
        assertEquals("java.awt.Color[r=129,g=133,b=131]", colorResult);
    }

    @Test
    void extractMainColor_WithNullImage() throws IOException {
        ToolsServiceImpl testService = new ToolsServiceImpl() {
            @Override
            public BufferedImage loadImage(String imageUrl) {
                return null;
            }
        };

        String colorResult = testService.extractMainColor("http://example.com/test.jpg");
        
        assertEquals("java.awt.Color[r=129,g=133,b=131]", colorResult);
    }

    @Test
    void extractMainColor_Failure() {
        // 创建一个始终抛出异常的ToolsServiceImpl子类
        ToolsServiceImpl testService = new ToolsServiceImpl() {
            @Override
            public BufferedImage loadImage(String imageUrl) throws IOException {
                throw new IOException("Failed to load image");
            }
        };

        // 执行测试
        String colorResult = testService.extractMainColor("http://example.com/test.jpg");

        // 验证返回默认颜色
        assertEquals("java.awt.Color[r=129,g=133,b=131]", colorResult);
    }

    private BufferedImage createTestImage(Color color, boolean isDeep) {
        // 创建一个简单的测试图片
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 100, 100);
        g2d.dispose();
        return image;
    }

    @Test
    void extractMainColor_WithMixedColors() throws IOException {
        // 创建一个混合颜色的测试图片
        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = testImage.createGraphics();
        
        // 绘制深色部分
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(0, 0, 50, 100);
        
        // 绘制浅色部分
        g2d.setColor(new Color(200, 200, 200));
        g2d.fillRect(50, 0, 50, 100);
        
        g2d.dispose();
        
        ToolsServiceImpl testService = new ToolsServiceImpl() {
            @Override
            public BufferedImage loadImage(String imageUrl) {
                return testImage;
            }
        };

        String colorResult = testService.extractMainColor("http://example.com/test.jpg");
        
        assertNotNull(colorResult);
        assertTrue(colorResult.startsWith("java.awt.Color"));
    }
} 