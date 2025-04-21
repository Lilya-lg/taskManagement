package com.example.taskManagement.service;
import com.example.taskManagement.entity.RequestType;
import com.example.taskManagement.entity.Task;
import com.example.taskManagement.entity.User;
import com.example.taskManagement.repository.TaskRepository;
import com.example.taskManagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    public void saveTask(Task task) {
        taskRepository.save(task);
    }
    public long countAllTasks() {
        return taskRepository.count();  // Uses the count() method provided by JpaRepository
    }
    public long countAllTasksByRequester(User requester) {
        return taskRepository.countByRequester(requester);  // Uses the count() method provided by JpaRepository
    }
    public long countAllTasksByAssignedUser(User user){
        return taskRepository.countByAssignedUser(user);
    }
    public long countTasksByStatusAndAssignedUser(String status,User user){
        return taskRepository.countByStatusAndAssignedUser(Task.Status.valueOf(status),user);
    }
    public List<Task> getTasksForItManager(String username) {
        // Find the IT_MANAGER's department type
        User itManager = userRepository.findByEmail(username); // Assuming login is by email
        if (itManager == null || !"IT_MANAGER".equals(itManager.getRole().name())) {
            throw new RuntimeException("Access denied: Not an IT_MANAGER");
        }

        // Fetch tasks for the IT_MANAGER's department with status IN_PROGRESS
        return taskRepository.findByStatusNotAndRequestType(Task.Status.COMPLETED, itManager.getRequestType());
    }
    public long countOverdueTasksForAssignedUser(User assignedUser){
        LocalDate today = LocalDate.now();
        return taskRepository.countByDueDateBeforeAndStatusNotAndAssignedUser(today, Task.Status.COMPLETED,assignedUser);
    }
    public long countTasksByStatus(String status) {
        return taskRepository.countByStatus(Task.Status.valueOf(status));
    }
    public long countTasksByStatusAndRequest_Type(String status,RequestType request_type) {
        return taskRepository.countByStatusAndRequestType(Task.Status.valueOf(status),request_type);
    }
    public long countTasksByStatusForUser(String status,User username) {
        return taskRepository.countByStatusAndRequester(Task.Status.valueOf(status),username);
    }
    public long countOverdueTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.countByDueDateBeforeAndStatusNot(today,  Task.Status.COMPLETED);
    }
    public long countOverdueTasksForTeam(RequestType request_type) {
        LocalDate today = LocalDate.now();
        return taskRepository.countByDueDateBeforeAndStatusNotAndRequestType(today,  Task.Status.COMPLETED,request_type);
    }
    public long countOverdueTasksForUser(User username) {
        LocalDate today = LocalDate.now();
        return taskRepository.countByDueDateBeforeAndStatusNotAndRequester(today,  Task.Status.COMPLETED,username);
    }
    public Task findById(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        return task.orElse(null);
    }
    public List<Task> getTasksByRequester(User requester) {
        return taskRepository.findByRequester(requester);
    }

    public List<Task> getTasksForItSpecialist(User username){
        return taskRepository.findByAssignedUser(username);
    }

    public long countAllTasksByRequestType(RequestType request_type){
        return taskRepository.countByRequestType(request_type);
    }
    public void sendForApproval(Task task) {
        if (task.getNeedApproval() && !"COMPLETED".equals(task.getStatus())) {
            // Logic to handle the approval process, e.g., update status, send notifications, etc.
            //task.setStatus("PENDING_APPROVAL");
            taskRepository.save(task);
        } else {
            throw new IllegalStateException("Task cannot be sent for approval.");
        }
    }
    public void deleteTaskById(Long id) {
        taskRepository.deleteById(id);
    }


}
