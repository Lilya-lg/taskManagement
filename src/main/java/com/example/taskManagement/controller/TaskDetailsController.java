package com.example.taskManagement.controller;
import com.example.taskManagement.entity.Task;
import com.example.taskManagement.entity.TaskComment;
import com.example.taskManagement.entity.User;
import com.example.taskManagement.repository.TaskCommentRepository;
import com.example.taskManagement.repository.TaskRepository;
import com.example.taskManagement.repository.UserRepository;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/task_details")
@Validated
public class TaskDetailsController {
    private final TaskRepository taskRepo;
    private final TaskCommentRepository commentRepo;
    private final UserRepository userRepo;

    public TaskDetailsController(TaskRepository taskRepo, TaskCommentRepository commentRepo, UserRepository userRepo) {
        this.taskRepo = taskRepo;
        this.commentRepo = commentRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        Task task = taskRepo.findById(id).orElseThrow();
        model.addAttribute("task", task);
        model.addAttribute("comments", commentRepo.findByTaskIdOrderByCreatedAtAsc(id));
        model.addAttribute("newComment", new CreateCommentForm());
        return "task-details"; // thymeleaf view name
    }

    @PostMapping("/{id}/comments")
    public String addComment(@PathVariable Long id,
                             @ModelAttribute("newComment") @Validated CreateCommentForm form,
                             Authentication auth) {
        Task task = taskRepo.findById(id).orElseThrow();

        // resolve current user
        String email = auth.getName();
        User author = userRepo.findByEmailIgnoreCase(email);

        TaskComment c = new TaskComment();
        c.setTask(task);
        c.setAuthor(author);
        c.setBody(form.getBody().trim());
        c.setProgressPercent(form.getProgressPercent());

        commentRepo.save(c);
        return "redirect:/tasks/{id}#comments";
    }

    @Data
    public static class CreateCommentForm {
        @NotBlank
        private String body;

        @Min(0) @Max(100)
        private Integer progressPercent;
    }
}

