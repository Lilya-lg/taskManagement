package com.example.taskManagement.controller;

import com.example.taskManagement.service.TaskService;
import com.example.taskManagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class DashboardController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model, Principal principal) {

        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("user", userService.getUserByEmail(username));
        }

        model.addAttribute("totalTasks", taskService.countAllTasks());
        model.addAttribute("tasksInProgress", taskService.countTasksByStatus("IN_PROGRESS"));
        model.addAttribute("completedTasks", taskService.countTasksByStatus("COMPLETED"));
        model.addAttribute("overdueTasks", taskService.countOverdueTasks());

        return "dashboard";
    }
}
