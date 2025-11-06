package com.example.taskManagement.controller;


import com.example.taskManagement.entity.User;
import com.example.taskManagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @GetMapping("/profile")
    public String getProfile(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        model.addAttribute("user", user);
        model.addAttribute("page", "profile");
        return "profile";
    }
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal,
                                 RedirectAttributes ra) {
        if (principal == null) {
            ra.addFlashAttribute("error", "Please sign in again.");
            return "redirect:/login";
        }

        // Load the domain user by the principal username (adjust if it's not email)
        String username = principal.getUsername();
        User user = userService.getUserByEmail(username);
        if (user == null) {
            ra.addFlashAttribute("error", "User not found for: " + username);
            return "redirect:/profile";
        }

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            ra.addFlashAttribute("error", "Current password is incorrect.");
            return "redirect:/profile";
        }

        // Validate new password
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "New passwords do not match.");
            return "redirect:/profile";
        }
        if (newPassword.length() < 8) {
            ra.addFlashAttribute("error", "New password must be at least 8 characters.");
            return "redirect:/profile";
        }

        // Save encoded password
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.saveUser(user);

        ra.addFlashAttribute("success", "Password updated.");
        return "redirect:/profile";
    }

}

