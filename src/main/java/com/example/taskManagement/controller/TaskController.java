package com.example.taskManagement.controller;

import com.example.taskManagement.entity.RequestType;
import com.example.taskManagement.entity.Task;
import com.example.taskManagement.entity.User;
import com.example.taskManagement.repository.TaskRepository;
import com.example.taskManagement.service.TaskService;
import com.example.taskManagement.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    @Value("${file.upload-dir}")
    private String uploadDir;
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;

    @GetMapping
    public String getAllTasks(Model model, Principal principal) {
        User user = new User();
        if (principal != null) {
            String currentUsername = principal.getName();
            user = userService.getUserByEmail(currentUsername);
        }

        List<Task> tasks = taskService.getTasksByRequester(user);

        model.addAttribute("tasks", tasks);

        return "task-list";
    }

    @GetMapping("/manager")
    public String viewTasksForManager(Model model, Authentication authentication) {
        // Get the logged-in user's email
        String username = authentication.getName();
        // Fetch tasks for the IT_MANAGER
        List<Task> tasks = taskService.getTasksForItManager(username);

        // Add tasks to the model
        model.addAttribute("tasks", tasks);

        return "task-list-department"; // Thymeleaf template
    }

    @GetMapping("/team")
    public String viewTasksForSpecialist(Model model, Principal principal) {
        // Get the logged-in user's email
        User username = new User();
        if (principal != null) {
            String currentUsername = principal.getName();
            username = userService.getUserByEmail(currentUsername);
        }
        // Fetch tasks for the IT_MANAGER
        List<Task> tasks = taskService.getTasksForItSpecialist(username);

        // Add tasks to the model
        model.addAttribute("tasks", tasks);

        return "task-list-department"; // Thymeleaf template
    }

    @GetMapping("/new")
    public String showTaskForm(Model model, Principal principal) {
        Task task = new Task();

        // Fetch all users to allow assigning tasks to users
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);

        if (principal != null) {
            String currentUsername = principal.getName();
            User requester = userService.getUserByEmail(currentUsername);
            task.setRequester(requester); // Bind requester to the task
        }
        task.setDueDate(LocalDate.now());
        task.setStatus(Task.Status.TODO);
        model.addAttribute("task", task);
        return "task-form";
    }

    // POST request to handle form submission for creating a new task
    @PostMapping("/new")
    public String createTask(@Valid @ModelAttribute("task") Task task, BindingResult result, Model model, Principal principal, @RequestParam("file") MultipartFile file, Authentication authentication) {
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
        } else {
            model.addAttribute("requester", null);  // Set to null if not logged in
        }

        if (!file.isEmpty()) {
            try {
                // Save the file to the local filesystem
                String fileName = file.getOriginalFilename();
                Path fileStorageLocation = Paths.get("C:/uploads").toAbsolutePath().normalize();
                logger.info("File storage location: " + fileStorageLocation);
                Files.createDirectories(fileStorageLocation);
                Path targetLocation = fileStorageLocation.resolve(fileName);
                logger.info("Target location: " + targetLocation);
                if (Files.exists(targetLocation)) {
                    Files.delete(targetLocation);
                }
                Files.copy(file.getInputStream(), targetLocation);

                // Store the file metadata in the task
                task.setFileName(fileName);
                task.setFilePath(targetLocation.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (task.getDueDate() == null) {
            task.setDueDate(LocalDate.now()); // Default to today if not set
        }
        taskService.saveTask(task);
        if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IT_MANAGER"))) {
            return "redirect:/tasks/manager";
        } else if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IT_TEAM"))) {
            return "redirect:/tasks/team";
        }
        return "redirect:/tasks";
    }

    @PostMapping("/edit/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute("task") Task updatedTask, @RequestParam("file") MultipartFile file, Authentication authentication) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setStatus(updatedTask.getStatus());
        task.setAssignedUser(updatedTask.getAssignedUser());
        task.setRequestType(updatedTask.getRequestType());
        task.setDueDate(updatedTask.getDueDate());
        task.setNeedApproval(updatedTask.getNeedApproval());

        if (!file.isEmpty()) {
            try {
                // Save the file to the local filesystem
                String fileName = file.getOriginalFilename();
                Path fileStorageLocation = Paths.get("C:/uploads").toAbsolutePath().normalize();
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
        //
        taskRepository.save(task);
        if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IT_MANAGER"))) {
            return "redirect:/tasks/manager";
        } else if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IT_TEAM"))) {
            return "redirect:/tasks/team";
        }
        return "redirect:/tasks";
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
            model.addAttribute("todo", Task.Status.TODO);
            RequestType requestType = RequestType.valueOf(task.getRequestType().toString());
            List<User> filteredUsers = userService.getFilteredUsers(User.Role.IT_TEAM, requestType);

            if (filteredUsers.isEmpty()) {
                System.out.println("No matching users found for task request type: " + task.getRequestType());
            }

            model.addAttribute("users", filteredUsers);
            return "task-edit";
        } else {
            return "error";
        }
    }


    @GetMapping("/file/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("C:/uploads").resolve(filename).normalize();
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

    @PostMapping("/send-for-approval/{id}")
    public String sendForApproval(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Task task = taskService.findById(id);
        if (task == null) {
            redirectAttributes.addFlashAttribute("error", "Task not found.");
            return "redirect:/tasks";
        }

        if (!task.getNeedApproval() || "COMPLETED".equals(task.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Task cannot be sent for approval.");
            return "redirect:/tasks";
        }

        // Update task status or send notification for approval
        taskService.sendForApproval(task);
        redirectAttributes.addFlashAttribute("success", "Task sent for approval successfully.");
        return "redirect:/tasks";
    }

    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            taskService.deleteTaskById(id);
            redirectAttributes.addFlashAttribute("message", "Task deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting task: " + e.getMessage());
        }
        if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IT_MANAGER"))) {
            return "redirect:/tasks/manager";
        } else if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IT_TEAM"))) {
            return "redirect:/tasks/team";
        }
        return "redirect:/tasks";
    }

}