package com.example.taskManagement.service;

import com.example.taskManagement.entity.Task;
import com.example.taskManagement.entity.User;
import com.example.taskManagement.repository.TaskRepository;
import com.example.taskManagement.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ITManagerTaskService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ITManagerTaskService(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public List<Task> getTasksForManager(String email) {
        User manager = userRepository.findByEmailIgnoreCase(email);
        if (manager == null || !"IT_MANAGER".equals(manager.getRole().name())) {
            throw new RuntimeException("Access denied: Not an IT_MANAGER");
        }
        List<Task.Status> excludedStatuses = List.of(Task.Status.COMPLETED, Task.Status.DELETED);
        return taskRepository.findByStatusNotInAndRequestType(excludedStatuses, manager.getRequestType());
    }
}
