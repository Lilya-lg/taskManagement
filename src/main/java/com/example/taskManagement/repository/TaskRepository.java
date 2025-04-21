package com.example.taskManagement.repository;

import com.example.taskManagement.entity.RequestType;
import com.example.taskManagement.entity.Task;
import com.example.taskManagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Method to count tasks by status
    long countByStatus(Task.Status status);

    long countByAssignedUser(User user);

    long countByStatusAndRequestType(Task.Status status, RequestType request_type);

    long countByRequestType(RequestType request_type);

    long countByRequester(User user);

    long countByStatusAndRequester(Task.Status status, User user);

    long countByStatusAndAssignedUser(Task.Status status, User user);

    List<Task> findByStatusNotAndRequestType(Task.Status status, RequestType requestType);

    long countByDueDateBeforeAndStatusNotAndAssignedUser(LocalDate date, Task.Status status, User user);

    // Method to count overdue tasks that are not completed
    long countByDueDateBeforeAndStatusNot(LocalDate date, Task.Status status);

    long countByDueDateBeforeAndStatusNotAndRequester(LocalDate date, Task.Status status, User user);

    List<Task> findByStatusAndRequestType(Task.Status status, RequestType requestType);

    List<Task> findByRequester(User user);

    List<Task> findByAssignedUser(User user);

    long countByDueDateBeforeAndStatusNotAndRequestType(LocalDate date, Task.Status status, RequestType request_type);

}

