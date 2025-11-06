package com.example.taskManagement.controller;


import com.example.taskManagement.entity.ChangeRequest;

import com.example.taskManagement.entity.User;
import com.example.taskManagement.service.ChangeRequestService;
import com.example.taskManagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/change-requests")
public class ChangeRequestController {

    private final ChangeRequestService changeRequestService;
    private final UserService userService;

    public ChangeRequestController(ChangeRequestService changeRequestService, UserService userService) {
        this.changeRequestService = changeRequestService;
        this.userService = userService;
    }

    @GetMapping
    public String getChangeRequests(Model model) {
        List<ChangeRequest> changeRequests = changeRequestService.getAllChangeRequests();
        model.addAttribute("changeRequests", changeRequests);
        model.addAttribute("page", "changes");
        return "change-request-list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('IT_TEAM','IT_MANAGER')")
    public String showForm(Model model) {
        ChangeRequest cr = new ChangeRequest();
        cr.setStatus(ChangeRequest.Status.DRAFT);

        model.addAttribute("cr", cr);
        model.addAttribute("tasks", changeRequestService.getCandidateTasks());
        model.addAttribute("page", "changes");
        return "change-request-form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasAnyRole('IT_TEAM','IT_MANAGER')")
    public String create(@Valid @ModelAttribute("cr") ChangeRequest cr,
                         BindingResult binding,
                         @RequestParam(value = "taskId", required = false) Long taskId,
                         RedirectAttributes ra,
                         Model model) {

        if (binding.hasErrors()) {
            // re-populate dropdowns on error
            model.addAttribute("tasks", changeRequestService.getCandidateTasks());
            model.addAttribute("page", "changes");
            return "change-request-form";
        }

        try {
            changeRequestService.create(cr, taskId);
            ra.addFlashAttribute("message", "Change Request created.");
            return "redirect:/change-requests";
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Could not create Change Request: " + e.getMessage());
            return "redirect:/change-requests/new";
        }
    }

    @GetMapping("/{id}")
    public String viewChangeRequest(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return changeRequestService.findById(id)
                .map(found -> {
                    model.addAttribute("changeRequest", found);
                    model.addAttribute("page", "changes");
                    return "change-request-view";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Change request not found");
                    return "redirect:/change-requests";
                });
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('IT_TEAM','IT_MANAGER')")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return changeRequestService.findById(id)
                .map(found -> {
                    model.addAttribute("cr", found);
                    model.addAttribute("tasks", changeRequestService.getCandidateTasks());
                    model.addAttribute("page", "changes");
                    return "change-request-edit";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Change request not found");
                    return "redirect:/change-requests";
                });
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('IT_TEAM','IT_MANAGER')")
    public String updateChangeRequest(@PathVariable Long id,
                                      @Valid @ModelAttribute("cr") ChangeRequest cr, // <-- "cr"
                                      BindingResult binding,                         // IMPORTANT: right after model attr
                                      @RequestParam(value = "taskId", required = false) Long taskId,
                                      RedirectAttributes ra,
                                      Model model) {
        if (binding.hasErrors()) {
            model.addAttribute("tasks", changeRequestService.getCandidateTasks());
            model.addAttribute("page", "changes");
            return "change-request-edit";
        }

        try {
            changeRequestService.updateChangeRequest(id, cr, taskId);
            ra.addFlashAttribute("message", "Change request updated successfully!");
            return "redirect:/change-requests";
        } catch (Exception e) {
            model.addAttribute("tasks", changeRequestService.getCandidateTasks());
            model.addAttribute("error", "Could not update: " + e.getMessage());
            return "change-request-edit";
        }
    }
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('IT_TEAM','IT_MANAGER')")
    public String submitForReview(@PathVariable Long id,
                                  Principal principal,
                                  RedirectAttributes ra) {
        try {
            User submitter = userService.getUserByEmail(principal.getName());
            changeRequestService.submitForReview(id, submitter);
            ra.addFlashAttribute("message", "Change Request submitted for review.");
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) root = root.getCause();
            ra.addFlashAttribute("error", root.getMessage());  // TEMP: show root cause
        }
        return "redirect:/change-requests/" + id;
    }

    // ========== Director review screen ==========
    @GetMapping("/{id}/review")
    @PreAuthorize("hasRole('IT_DIRECTOR')")
    public String reviewPage(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return changeRequestService.findById(id)
                .map(cr -> {
                    if (cr.getStatus() != ChangeRequest.Status.UNDER_REVIEW) {
                        ra.addFlashAttribute("error", "Only requests under review can be processed.");
                        return "redirect:/change-requests/" + id;
                    }
                    model.addAttribute("cr", cr);
                    model.addAttribute("page", "changes");
                    return "change-request-review"; // template below
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("error", "Change request not found.");
                    return "redirect:/change-requests";
                });
    }

    // ========== Director actions ==========
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('IT_DIRECTOR')")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false) String comment,
                          Principal principal,
                          RedirectAttributes ra) {
        try {
            User director = userService.getUserByEmail(principal.getName());
            changeRequestService.approve(id, director, comment);
            ra.addFlashAttribute("message", "Approved.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/change-requests/" + id;
    }

    @PostMapping("/{id}/decline")
    @PreAuthorize("hasRole('IT_DIRECTOR')")
    public String decline(@PathVariable Long id,
                          @RequestParam String comment,
                          Principal principal,
                          RedirectAttributes ra) {
        try {
            if (comment == null || comment.isBlank()) {
                throw new IllegalArgumentException("Decline comment is required.");
            }
            User director = userService.getUserByEmail(principal.getName());
            changeRequestService.decline(id, director, comment);
            ra.addFlashAttribute("message", "Declined.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/change-requests/" + id;
    }

}

