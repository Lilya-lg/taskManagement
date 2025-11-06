package com.example.taskManagement.controller;

import com.example.taskManagement.entity.User;
import com.example.taskManagement.service.TaskQueryService;
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
    private TaskQueryService taskQueryService;

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
            model.addAttribute("totalTasks", taskQueryService.countByRequester(usernameU));
            model.addAttribute("tasksInProgress", taskQueryService.countByStatusForRequester("IN_PROGRESS", usernameU));
            model.addAttribute("completedTasks", taskQueryService.countByStatusForRequester("COMPLETED", usernameU));
            model.addAttribute("overdueTasks", taskQueryService.countOverdueForRequester(usernameU));
        } else if (hasTeamRole) {
            model.addAttribute("totalTasks", taskQueryService.countByAssignedUser(usernameU));
            model.addAttribute("tasksInProgress", taskQueryService.countByStatusAndAssignedUser("IN_PROGRESS", usernameU));
            model.addAttribute("completedTasks", taskQueryService.countByStatusAndAssignedUser("COMPLETED", usernameU));
            model.addAttribute("overdueTasks", taskQueryService.countOverdueForAssignedUser(usernameU));
        } else if (hasManagerRole) {
            model.addAttribute("totalTasks", taskQueryService.countByRequestType(usernameU.getRequestType()));
            model.addAttribute("tasksInProgress", taskQueryService.countByStatusAndRequestType("IN_PROGRESS", usernameU.getRequestType()));
            model.addAttribute("completedTasks", taskQueryService.countByStatusAndRequestType("COMPLETED", usernameU.getRequestType()));
            model.addAttribute("overdueTasks", taskQueryService.countOverdueForRequestType(usernameU.getRequestType()));
        } else if (hasDirectorRole) {
            model.addAttribute("totalTasks", taskQueryService.countAllTasks());
            model.addAttribute("tasksInProgress", taskQueryService.countByStatus("IN_PROGRESS"));
            model.addAttribute("completedTasks", taskQueryService.countByStatus("COMPLETED"));
            model.addAttribute("overdueTasks", taskQueryService.countOverdueTasks());
        }
        model.addAttribute("page", "dashboard");
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
