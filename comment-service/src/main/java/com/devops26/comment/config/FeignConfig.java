package com.devops26.comment.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Configuration
public class FeignConfig implements RequestInterceptor {



    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("token", getHttpServletRequest().getHeader("token"));

    }

    private HttpServletRequest getHttpServletRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    private Map<String, String> getHeaders(HttpServletRequest request) {
//        Map<String, String> map = new LinkedHashMap<>();
//        Enumeration<String> enumeration = request.getHeaderNames();
//        while (enumeration.hasMoreElements()) {
//            String key = enumeration.nextElement();
//            String value = request.getHeader(key);
//            map.put(key, value);
//        }
//        return map;
//    }
}


