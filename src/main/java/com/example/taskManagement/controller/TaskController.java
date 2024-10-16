package com.example.taskManagement.controller;

import com.example.taskManagement.entity.Task;
import com.example.taskManagement.entity.User;
import com.example.taskManagement.repository.TaskRepository;
import com.example.taskManagement.service.TaskService;
import com.example.taskManagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;
    @GetMapping
    public String getAllTasks(Model model) {
        List<Task> tasks = taskService.getAllTasks();

        model.addAttribute("tasks", tasks);

        return "task-list";
    }

    @GetMapping("/new")
    public String showTaskForm(Model model, Principal principal) {
        model.addAttribute("task", new Task());

        // Fetch all users to allow assigning tasks to users
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);


        if (principal != null) {
            String currentUsername = principal.getName();
            User requester = userService.getUserByEmail(currentUsername);

            model.addAttribute("requesterName", requester!=null ? requester.getFirstName() + " " + requester.getLastName() : "");

        } else {
            model.addAttribute("requesterName", "Anonymous");
        }
        return "task-form";
    }

    // POST request to handle form submission for creating a new task
    @PostMapping("/new")
    public String createTask(@Valid @ModelAttribute("task") Task task, BindingResult result, Model model,Principal principal) {
        // Check for validation errors
        if (result.hasErrors()) {
            // If there are errors, return the form view with validation messages
            model.addAttribute("users", userService.getAllUsers()); // Keep user list in the form
            return "task-form";
        }
        if (principal != null) {
            String currentUsername = principal.getName(); // Get username of the logged-in user
            User requester = userService.getUserByEmail(currentUsername);  // Assuming email is used as username
            task.setRequester(requester);  // Set the current user as the requester
        }
        else {
            model.addAttribute("requester", null);  // Set to null if not logged in
        }
        taskService.saveTask(task);

        return "redirect:/tasks";
    }

    @PostMapping("/edit/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute("task") Task updatedTask,@RequestParam("file") MultipartFile file) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        task.setAssignedUser(updatedTask.getAssignedUser());
        task.setRequestType(updatedTask.getRequestType());
       // task.setDueDate(myDate);

        if (!file.isEmpty()) {
            try {
                // Save the file to the local filesystem
                String fileName = file.getOriginalFilename();
                Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
                Files.createDirectories(fileStorageLocation);
                Path targetLocation = fileStorageLocation.resolve(fileName);
                Files.copy(file.getInputStream(), targetLocation);

                // Store the file metadata in the task
                task.setFileName(fileName);
                task.setFilePath(targetLocation.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        taskRepository.save(task);
        return "redirect:/tasks";  // Redirect back to task list
    }

    @GetMapping("/{id}")
    public String viewTask(@PathVariable("id") Long id, Model model) {
        Task task = taskService.findById(id);
        if (task != null) {
            model.addAttribute("task", task);
            return "task-view";
        } else {
            return "error";
        }
    }
    @GetMapping("/edit/{id}")
    public String editTask(@PathVariable("id") Long id, Model model) {
        Task task = taskService.findById(id);
        if (task != null) {
            List<User> users = userService.getAllUsers();


            String formattedDate = task.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            model.addAttribute("myDate", formattedDate);
            System.out.println(formattedDate);

            model.addAttribute("task", task);
            model.addAttribute("users", users);
            return "task-edit";
        } else {
            return "error";
        }
    }


    @GetMapping("/file/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads").resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("File not found");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error serving file", ex);
        }
    }
}