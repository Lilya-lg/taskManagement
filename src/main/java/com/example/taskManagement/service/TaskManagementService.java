package com.example.taskManagement.service;

import com.example.taskManagement.entity.Task;
import com.example.taskManagement.repository.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskManagementService {
    private final TaskRepository taskRepository;

    public TaskManagementService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public void save(Task task) {
        taskRepository.save(task);
    }

    public void deleteById(Long id) {
        Task t = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (t.getStatus() != Task.Status.TODO) {
            throw new IllegalStateException("Only TODO tasks can be deleted. Current status: " + t.getStatus());
        }
        t.setStatus(Task.Status.DELETED);
        taskRepository.save(t);
        //taskRepository.deleteById(id);
    }

    public void sendForApproval(Task task) {
        if (task.getNeedApproval() && task.getStatus() != Task.Status.COMPLETED) {
            taskRepository.save(task);
        } else {
            throw new IllegalStateException("Task cannot be sent for approval.");
        }
    }
}
