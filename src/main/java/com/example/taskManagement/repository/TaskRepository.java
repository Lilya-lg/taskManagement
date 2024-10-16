package com.example.taskManagement.repository;

import com.example.taskManagement.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Method to count tasks by status
    long countByStatus(Task.Status status);

    // Method to count overdue tasks that are not completed
    long countByDueDateBeforeAndStatusNot(LocalDate date, Task.Status status);
}

