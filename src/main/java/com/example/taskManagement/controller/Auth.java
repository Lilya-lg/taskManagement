package com.example.taskManagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
class Auth {
    @GetMapping("/login")
    String login() {
        return "login";
    }
}