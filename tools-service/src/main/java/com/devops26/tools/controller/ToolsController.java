package com.devops26.tools.controller;

import com.devops26.tools.entity.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.devops26.tools.service.ToolsService;

@RestController
@RequestMapping("/tools")
public class ToolsController {

    @Autowired
    private ToolsService toolsService;

    @PostMapping("/upload")
    public ResultVO<String> upload(@RequestParam("file") MultipartFile file){
        return ResultVO.buildSuccess(toolsService.upload(file));
    }

    @GetMapping("/extractMainColor")
    public ResultVO<String> extractMainColor(@RequestParam("imageUrl") String imageUrl){
        return ResultVO.buildSuccess(toolsService.extractMainColor(imageUrl));
    }


}