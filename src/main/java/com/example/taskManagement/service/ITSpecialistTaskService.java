package com.example.taskManagement.service;

import com.example.taskManagement.entity.Task;
import com.example.taskManagement.entity.User;
import com.example.taskManagement.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ITSpecialistTaskService {
    private final TaskRepository taskRepository;

    public ITSpecialistTaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getTasksForSpecialist(User specialist) {
        return taskRepository.findByAssignedUser(specialist);
    }
    public List<Task> getTasksForSpecialistWithAssigned(User specialist) {
        return taskRepository.findByAssignedUserOrRequester(specialist,specialist);
    }

}
