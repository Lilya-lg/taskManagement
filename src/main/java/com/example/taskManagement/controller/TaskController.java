package com.example.taskManagement.controller;

import com.example.taskManagement.entity.RequestType;
import com.example.taskManagement.entity.Task;
import com.example.taskManagement.entity.User;
import com.example.taskManagement.event.TaskAssignedUserChangedEvent;
import com.example.taskManagement.event.TaskStatusChangedEvent;
import com.example.taskManagement.repository.TaskCommentRepository;
import com.example.taskManagement.repository.TaskRepository;
import com.example.taskManagement.service.*;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.List;

import org.slf4j.Logger;


@Controller
@RequestMapping("/tasks")
public class TaskController {
    @Value("${file.upload-dir}")
    private  String uploadDir;
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskRepository taskRepository;
    private final TaskQueryService taskQueryService;
    private final TaskManagementService taskManagementService;
    private final ITSpecialistTaskService itSpecialistTaskService;
    private final ITManagerTaskService itManagerTaskService;
    private final UserService userService;
    private final TaskCommentRepository commentRepo;
    private final ApplicationEventPublisher events;

    public TaskController(TaskRepository taskRepository, TaskQueryService taskQueryService, TaskManagementService taskManagementService, ITSpecialistTaskService itSpecialistTaskService, ITManagerTaskService itManagerTaskService, UserService userService, TaskCommentRepository commentRepo, ApplicationEventPublisher events) {
        this.taskRepository = taskRepository;
        this.taskQueryService = taskQueryService;
        this.taskManagementService = taskManagementService;
        this.itSpecialistTaskService = itSpecialistTaskService;
        this.itManagerTaskService = itManagerTaskService;
        this.userService = userService;
        this.commentRepo = commentRepo;
        this.events = events;
    }


    @GetMapping
    public String getAllTasks(@RequestParam(required = false) String status,
                              @RequestParam(required = false) RequestType requestType,
                              @RequestParam(required = false) Long assignedUser,
                              Model model,
                              Principal principal,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size){
        User requester = null;
        if (principal != null) {
            requester = userService.getUserByEmail(principal.getName());
        }

        Task.Status statusEnum = null;
        if (status != null && !status.isBlank()) {
            if ("CLOSED".equalsIgnoreCase(status)) status = "COMPLETED";
            statusEnum = Task.Status.valueOf(status);
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Task> taskPage = taskQueryService.findByFiltersForRequesterPageable(
                requester, statusEnum, requestType, assignedUser, pageable);
        List<User> users = userService.getAllUsers();

        model.addAttribute("tasks", taskPage.getContent());
        model.addAttribute("users", users);

        model.addAttribute("filterStatus", status);
        model.addAttribute("filterRequestType", requestType);
        model.addAttribute("filterAssignedUser", assignedUser);

        model.addAttribute("page", "tasks");

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", taskPage.getTotalPages());

        return "task-list";
    }


    @GetMapping("/manager")
    public String viewTasksForManager(Model model, Authentication authentication, @RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        String username = authentication.getName();
        List<Task> tasks = itManagerTaskService.getTasksForManager(username);
        model.addAttribute("tasks", tasks);
        model.addAttribute("page", "tasks-manager");
        return "task-list-department";
    }

    @GetMapping("/director")
    public String viewTasksForDirector(@RequestParam(required = false) String status,
                                       @RequestParam(required = false) RequestType requestType,
                                       @RequestParam(required = false) Long assignedUser,
            Model model, Principal principal, @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size) {
        User username = new User();
        if (principal != null) {
            String currentUsername = principal.getName();
            username = userService.getUserByEmail(currentUsername);
        }
        Task.Status statusEnum = null;
        if (status != null && !status.isBlank()) {
            if ("CLOSED".equalsIgnoreCase(status)) status = "COMPLETED";
            statusEnum = Task.Status.valueOf(status);
        }
        List<Task> tasks = taskQueryService.findByFiltersForAll(
                statusEnum,
                requestType,
                assignedUser
        );

        List<User> users = userService.getAllUsers();
        model.addAttribute("tasks", tasks);
        model.addAttribute("page", "tasks-director");
        return "task-list-director";
    }


    @GetMapping("/team")
    public String viewTasksForSpecialist(@RequestParam(required = false) String status,
                                         @RequestParam(required = false) RequestType requestType,
                                         @RequestParam(required = false) Long assignedUser,
                                         Model model,
                                         Principal principal, @RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        User username = new User();
        if (principal != null) {
            String currentUsername = principal.getName();
            username = userService.getUserByEmail(currentUsername);
        }
        Task.Status statusEnum = null;
        if (status != null && !status.isBlank()) {
            if ("CLOSED".equalsIgnoreCase(status)) status = "COMPLETED";
            statusEnum = Task.Status.valueOf(status);
        }
        List<Task> tasks = taskQueryService.findByFiltersForAssignee(
                username,
                statusEnum,
                assignedUser
        );
        List<User> users = userService.getAllUsers();

        model.addAttribute("tasks", tasks);
        model.addAttribute("users", users);

        model.addAttribute("filterStatus", status);
        model.addAttribute("filterRequestType", requestType);
        model.addAttribute("filterAssignedUser", assignedUser);

        // Active sidebar item
        model.addAttribute("page", "tasks-team");

        return "task-list-department";
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
            task.setRequester(requester);
        }
        task.setDueDate(LocalDate.now());
        task.setStatus(Task.Status.TODO);
        model.addAttribute("task", task);
        return "task-form";
    }

    @PostMapping("/new")
    public String createTask(@Valid @ModelAttribute("task") Task task, BindingResult result, Model model,
                             Principal principal, @RequestParam("file") MultipartFile file,
                             Authentication authentication) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.getAllUsers());
            return "task-form";
        }
        if (principal != null) {
            String currentUsername = principal.getName();
            User requester = userService.getUserByEmail(currentUsername);
            task.setRequester(requester);
        } else {
            model.addAttribute("requester", null);
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
            task.setDueDate(LocalDate.now());
        }
        taskManagementService.save(task);
        if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IT_MANAGER"))) {
            return "redirect:/tasks/manager";
        } else if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IT_TEAM"))) {
            return "redirect:/tasks/team";
        }
     else if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority(
            "ROLE_IT_DIRECTOR"))) {
        return "redirect:/tasks/director";
    }
        return "redirect:/tasks";
    }

    @PostMapping("/edit/{id}")
    public String updateTask(@PathVariable Long id, @ModelAttribute("task") Task updatedTask,
                             @RequestParam("file") MultipartFile file, Authentication authentication) {
        Task task = taskQueryService.findById(id);
        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        Task.Status oldStatus = task.getStatus();
        User oldUser = task.getAssignedUser();
        task.setStatus(updatedTask.getStatus());
        task.setAssignedUser(updatedTask.getAssignedUser());
        task.setRequestType(updatedTask.getRequestType());
        task.setDueDate(updatedTask.getDueDate());
        task.setNeedApproval(updatedTask.getNeedApproval());

        if (!file.isEmpty()) {
            try {
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
        if(!oldStatus.equals(updatedTask.getStatus())) {
            events.publishEvent(new TaskStatusChangedEvent(task.getId(), oldStatus.toString(),
                    updatedTask.getStatus().toString()));
        }
        if(oldUser==null) {
            events.publishEvent(new TaskAssignedUserChangedEvent(task.getId(), task.getAssignedUser()));
        }
        else if (!oldUser.equals(task.getAssignedUser())) {
                events.publishEvent(new TaskAssignedUserChangedEvent(task.getId(), task.getAssignedUser()));
        }
        taskManagementService.save(task);
        if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IT_MANAGER"))) {
            return "redirect:/tasks/manager";
        } else if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_IT_TEAM"))) {
            return "redirect:/tasks/team";
        }
        else if (authentication != null && authentication.getAuthorities().contains(new SimpleGrantedAuthority(
                "ROLE_IT_DIRECTOR"))) {
            return "redirect:/tasks/director";
        }
        return "redirect:/tasks";
    }

    @GetMapping("/{id}")
    public String viewTask(@PathVariable("id") Long id, Model model) {
        Task task = taskQueryService.findById(id);
        if (task != null) {
            model.addAttribute("task", task);
            return "task-view";
        } else {
            return "error";
        }
    }

    @GetMapping("/edit/{id}")
    public String editTask(@PathVariable("id") Long id, Model model) {
        Task task = taskQueryService.findById(id);
        if (task != null) {
            List<User> users = userService.getAllUsers();
            String formattedDate = task.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            model.addAttribute("myDate", formattedDate);

            model.addAttribute("task", task);
            model.addAttribute("todo", Task.Status.TODO);
            RequestType requestType = RequestType.valueOf(task.getRequestType().toString());
            List<User> filteredUsers = userService.getFilteredUsers(User.Role.IT_TEAM, User.Role.IT_MANAGER);

            if (filteredUsers.isEmpty()) {
                System.out.println("No matching users found for task request type: " + task.getRequestType());
            }

            model.addAttribute("users", filteredUsers);
            model.addAttribute("comments", commentRepo.findByTaskIdOrderByCreatedAtAsc(id));
            model.addAttribute("newComment", new TaskDetailsController.CreateCommentForm());
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
        Task task = taskQueryService.findById(id);
        if (task == null) {
            redirectAttributes.addFlashAttribute("error", "Task not found.");
            return "redirect:/tasks";
        }

        if (!task.getNeedApproval() || "COMPLETED".equals(task.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Task cannot be sent for approval.");
            return "redirect:/tasks";
        }

        // Update task status or send notification for approval
        taskManagementService.sendForApproval(task);
        redirectAttributes.addFlashAttribute("success", "Task sent for approval successfully.");
        return "redirect:/tasks";
    }

    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttributes, Authentication authentication) {
        try {
            taskManagementService.deleteById(id);
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