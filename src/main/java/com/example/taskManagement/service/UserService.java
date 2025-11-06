package com.example.taskManagement.service;
import com.example.taskManagement.entity.User;
import com.example.taskManagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public List<User> getFilteredUsers(User.Role role, User.Role role1) {
        return userRepository.findUsersByRoleAndRequestType(role, role1);
    }
}

