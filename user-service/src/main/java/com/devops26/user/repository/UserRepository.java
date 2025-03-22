package com.devops26.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.devops26.user.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUserId(Integer userId);
    User findByPhoneAndPassword(String phone, String password);
    User findByName(String name);
    User findByPhone(String phone);

}