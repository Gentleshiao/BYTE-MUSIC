package com.devops26.tools.service;

import org.springframework.web.multipart.MultipartFile;

public interface ToolsService {
    String upload(MultipartFile file);
    String extractMainColor(String imageUrl);
}