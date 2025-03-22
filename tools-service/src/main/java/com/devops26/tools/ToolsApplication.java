package com.devops26.tools;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ToolsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ToolsApplication.class, args);
    }
} 