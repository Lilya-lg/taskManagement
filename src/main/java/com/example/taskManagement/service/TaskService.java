package com.example.taskManagement.service;
import com.example.taskManagement.entity.Task;
import com.example.taskManagement.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    public void saveTask(Task task) {
        taskRepository.save(task);
    }
    public long countAllTasks() {
        return taskRepository.count();  // Uses the count() method provided by JpaRepository
    }


    public long countTasksByStatus(String status) {
        return taskRepository.countByStatus(Task.Status.valueOf(status));
    }

    public long countOverdueTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.countByDueDateBeforeAndStatusNot(today,  Task.Status.COMPLETED);
    }
    public Task findById(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        return task.orElse(null);
    }

}
