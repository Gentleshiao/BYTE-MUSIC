package com.devops26.tools.service.impl;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.devops26.tools.exception.TuneIslandException;
import com.devops26.tools.util.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.devops26.tools.service.ToolsService;

@Service
@Slf4j
public class ToolsServiceImpl implements ToolsService {

    @Autowired
    OssUtil obsUtil;

    @Override
    public String upload(MultipartFile file) {
        try {
            String url = obsUtil.upload(file.getOriginalFilename(), file.getInputStream());
            log.info("Successfully uploaded file: {}", file.getOriginalFilename());
            return url;
        } catch (Exception e) {
            log.error("Failed to upload file {}: {}", file.getOriginalFilename(), e.getMessage());
            throw TuneIslandException.uploadFailed();
        }
    }

    @Override
    @Cacheable(value = "mainColor", key = "#imageUrl")
    public String extractMainColor(String imageUrl) {
        try {
            BufferedImage image = loadImage(imageUrl);
            if (image == null) {
                throw new IOException("无法读取图片");
            }

            ColorStatistics stats = calculateColorStatistics(image);
            Color resultColor = determineMainColor(stats);
            log.info("Successfully extracted main color from image: {}", imageUrl);
            return resultColor.toString();
        } catch (Exception e) {
            log.error("Failed to extract main color from image {}: {}", imageUrl, e.getMessage());
            return new Color(129,133,131).toString();
        }
    }

    private Color determineMainColor(ColorStatistics stats) {
        if (isLightColor(stats.avgRed, stats.avgGreen, stats.avgBlue) && stats.deepPixelCount > 0) {
            return new Color(
                    (int) (stats.deepSumRed / stats.deepPixelCount),
                    (int) (stats.deepSumGreen / stats.deepPixelCount),
                    (int) (stats.deepSumBlue / stats.deepPixelCount)
            );
        }
        if (stats.deepPixelCount == 0) {
            return new Color(129,133,131);
        }
        return new Color(stats.avgRed, stats.avgGreen, stats.avgBlue);
    }

    public BufferedImage loadImage(String imageUrl) throws IOException, ImageReadException {
        InputStream inputStream = new URL(imageUrl).openStream();
        return Imaging.getBufferedImage(inputStream);
    }

    private ColorStatistics calculateColorStatistics(BufferedImage image) {
        ColorAccumulator accumulator = new ColorAccumulator();
        processImagePixels(image, accumulator);
        return calculateAverageColor(accumulator);
    }

    private void processImagePixels(BufferedImage image, ColorAccumulator accumulator) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                processPixel(image.getRGB(x, y), accumulator);
            }
        }
    }

    private void processPixel(int rgb, ColorAccumulator accumulator) {
        PixelAnalysisResult result = analyzePixel(new Color(rgb));
        accumulator.addPixel(result);
    }

    private static class ColorAccumulator {
        long sumRed = 0, sumGreen = 0, sumBlue = 0;
        long deepSumRed = 0, deepSumGreen = 0, deepSumBlue = 0;
        int pixelCount = 0, deepPixelCount = 0;

        void addPixel(PixelAnalysisResult pixel) {
            // 累加普通像素的颜色信息
            sumRed += pixel.red;
            sumGreen += pixel.green;
            sumBlue += pixel.blue;
            pixelCount++;

            // 如果是深色调，累加深色调的颜色信息
            if (pixel.isDeep) {
                deepSumRed += pixel.red;
                deepSumGreen += pixel.green;
                deepSumBlue += pixel.blue;
                deepPixelCount++;
            }
        }
    }

    private ColorStatistics calculateAverageColor(ColorAccumulator accumulator) {
        int avgRed = (int) (accumulator.sumRed / accumulator.pixelCount);
        int avgGreen = (int) (accumulator.sumGreen / accumulator.pixelCount);
        int avgBlue = (int) (accumulator.sumBlue / accumulator.pixelCount);

        return new ColorStatistics(
                avgRed, avgGreen, avgBlue,
                accumulator.deepSumRed, accumulator.deepSumGreen, accumulator.deepSumBlue,
                accumulator.deepPixelCount
        );
    }

    private PixelAnalysisResult analyzePixel(Color color) {
        // 分析每个像素的色调值，确定是否为深色调
        float[] pixelHSB = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        boolean isDeep = pixelHSB[2] < 0.7; // 判断是否为深色调
        return new PixelAnalysisResult(color.getRed(), color.getGreen(), color.getBlue(), isDeep);
    }

    private static class PixelAnalysisResult {
        int red, green, blue;
        boolean isDeep;

        PixelAnalysisResult(int red, int green, int blue, boolean isDeep) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.isDeep = isDeep;
        }
    }

    private boolean isLightColor(int red, int green, int blue) {
        float[] hsb = Color.RGBtoHSB(red, green, blue, null);
        return hsb[2] >= 0.7;
    }

    private static class ColorStatistics {
        int avgRed, avgGreen, avgBlue;
        long deepSumRed, deepSumGreen, deepSumBlue;
        int deepPixelCount;

        ColorStatistics(int avgRed, int avgGreen, int avgBlue, long deepSumRed, long deepSumGreen, long deepSumBlue, int deepPixelCount) {
            this.avgRed = avgRed;
            this.avgGreen = avgGreen;
            this.avgBlue = avgBlue;
            this.deepSumRed = deepSumRed;
            this.deepSumGreen = deepSumGreen;
            this.deepSumBlue = deepSumBlue;
            this.deepPixelCount = deepPixelCount;
        }
    }


}