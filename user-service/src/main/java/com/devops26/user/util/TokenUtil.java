package com.devops26.user.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.devops26.user.entity.User;
import com.devops26.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TokenUtil {
    private static final long EXPIRE_TIME = 365L * 24 * 60 * 60 * 30;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HttpServletRequest httpServletRequest;

    public String getToken(User user) {
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        return JWT.create()
                .withAudience(String.valueOf(user.getUserId()))
                .withExpiresAt(date)
                .sign(Algorithm.HMAC256(user.getPassword()));
    }

    public boolean verifyToken(String token) {
        try {
            Integer userId = Integer.parseInt(JWT.decode(token).getAudience().get(0));
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return false;
            }
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
            jwtVerifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public User getUser(String token) {
        try {
            Integer userId = Integer.parseInt(JWT.decode(token).getAudience().get(0));
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public User getCurrentUser() {
        try {
            String token = httpServletRequest.getHeader("token");
            return getUser(token);
        } catch (Exception e) {
            return null;
        }
    }
} 