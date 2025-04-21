package com.example.taskManagement.controller;

import com.example.taskManagement.entity.User;
import com.example.taskManagement.service.TaskService;
import com.example.taskManagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        User usernameU = new User();
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("user", userService.getUserByEmail(username));
            usernameU = userService.getUserByEmail(username);
        }


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean hasUserRole = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_USER"));

        boolean hasTeamRole = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_IT_TEAM"));

        boolean hasManagerRole = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_IT_MANAGER"));
        boolean hasDirectorRole = authentication.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_IT_DIRECTOR"));
        if (hasUserRole) {
            model.addAttribute("totalTasks", taskService.countAllTasksByRequester(usernameU));
            model.addAttribute("tasksInProgress", taskService.countTasksByStatusForUser("IN_PROGRESS", usernameU));
            model.addAttribute("completedTasks", taskService.countTasksByStatusForUser("COMPLETED", usernameU));
            model.addAttribute("overdueTasks", taskService.countOverdueTasksForUser(usernameU));
        } else if (hasTeamRole) {
            model.addAttribute("totalTasks", taskService.countAllTasksByAssignedUser(usernameU));
            model.addAttribute("tasksInProgress", taskService.countTasksByStatusAndAssignedUser("IN_PROGRESS", usernameU));
            model.addAttribute("completedTasks", taskService.countTasksByStatusAndAssignedUser("COMPLETED", usernameU));
            model.addAttribute("overdueTasks", taskService.countOverdueTasksForAssignedUser(usernameU));
        } else if (hasManagerRole) {
            model.addAttribute("totalTasks", taskService.countAllTasksByRequestType(usernameU.getRequestType()));
            model.addAttribute("tasksInProgress", taskService.countTasksByStatusAndRequest_Type("IN_PROGRESS", usernameU.getRequestType()));
            model.addAttribute("completedTasks", taskService.countTasksByStatusAndRequest_Type("COMPLETED", usernameU.getRequestType()));
            model.addAttribute("overdueTasks", taskService.countOverdueTasksForTeam(usernameU.getRequestType()));
        } else if (hasDirectorRole) {
            model.addAttribute("totalTasks", taskService.countAllTasks());
            model.addAttribute("tasksInProgress", taskService.countTasksByStatus("IN_PROGRESS"));
            model.addAttribute("completedTasks", taskService.countTasksByStatus("COMPLETED"));
            model.addAttribute("overdueTasks", taskService.countOverdueTasks());
        }
        return "dashboard";
    }

    @GetMapping("/debug")
    public String debugRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            System.out.println("User roles: " + authentication.getAuthorities());
        }
        return "test";
    }
}
