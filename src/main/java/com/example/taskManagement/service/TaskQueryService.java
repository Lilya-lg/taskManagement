package com.example.taskManagement.service;

import com.example.taskManagement.entity.RequestType;
import com.example.taskManagement.entity.Task;
import com.example.taskManagement.entity.User;
import com.example.taskManagement.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.example.taskManagement.service.TaskSpecifications.*;


@Service
public class TaskQueryService {
    private final TaskRepository taskRepository;

    public TaskQueryService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public long countAllTasks() {
        return taskRepository.count();
    }

    public long countByRequester(User requester) {
        return taskRepository.countByRequester(requester);
    }

    public long countByAssignedUser(User user) {
        return taskRepository.countByAssignedUser(user);
    }

    public long countByStatusAndAssignedUser(String status, User user) {
        return taskRepository.countByStatusAndAssignedUser(Task.Status.valueOf(status), user);
    }

    public long countByStatus(String status) {
        return taskRepository.countByStatus(Task.Status.valueOf(status));
    }

    public long countByStatusAndRequestType(String status, RequestType requestType) {
        return taskRepository.countByStatusAndRequestType(Task.Status.valueOf(status), requestType);
    }

    public long countByStatusForRequester(String status, User requester) {
        return taskRepository.countByStatusAndRequester(Task.Status.valueOf(status), requester);
    }

    public long countOverdueTasks() {
        return taskRepository.countByDueDateBeforeAndStatusNot(LocalDate.now(), Task.Status.COMPLETED);
    }

    public long countOverdueForAssignedUser(User user) {
        return taskRepository.countByDueDateBeforeAndStatusNotAndAssignedUser(LocalDate.now(), Task.Status.COMPLETED, user);
    }

    public long countOverdueForRequester(User user) {
        return taskRepository.countByDueDateBeforeAndStatusNotAndRequester(LocalDate.now(), Task.Status.COMPLETED, user);
    }

    public long countOverdueForRequestType(RequestType type) {
        return taskRepository.countByDueDateBeforeAndStatusNotAndRequestType(LocalDate.now(), Task.Status.COMPLETED, type);
    }

    public long countByRequestType(RequestType type) {
        return taskRepository.countByRequestType(type);
    }

    public Task findById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public List<Task> findByRequester(User requester) {
        return taskRepository.findByRequester(requester);
    }

    public List<Task> findByAssignedUser(User user) {
        return taskRepository.findByAssignedUser(user);
    }
    public List<Task> findByFiltersForRequester(User requester,
                                                Task.Status status,
                                                RequestType requestType,
                                                Long assignedUserId) {
        Specification<Task> spec = Specification
                .where(requesterIs(requester))
                .and(statusIs(status))
                .and(requestTypeIs(requestType))
                .and(assignedUserIdIs(assignedUserId));

        Sort sort = Sort.by(Sort.Direction.ASC, "dueDate").and(Sort.by("id"));
        return taskRepository.findAll(spec, sort);
    }
    public List<Task> findByFiltersForAssignee(User assignee,
                                               Task.Status status,
                                               Long assignedUserId) {
        if (assignee == null) {
            return Collections.emptyList();
        }
        List<Task> tasks = taskRepository.findActiveTasksForUser(assignee);

        if (status != null) {
            tasks = tasks.stream()
                    .filter(task -> task.getStatus() == status)
                    .toList();
        }
        return tasks.stream()
                .sorted(Comparator
                        .comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Task::getId))
                .toList();
    }
    public List<Task> findByFiltersForAll(     Task.Status status,
                                               RequestType requestType,
                                               Long assignedUserId) {
        Specification<Task> spec = Specification
                .where(TaskSpecifications.statusIs(status))
                .and(TaskSpecifications.requestTypeIs(requestType))
                .and(TaskSpecifications.assignedUserIdIs(assignedUserId))
                .and(TaskSpecifications.statusNot(Task.Status.DELETED));
        Sort sort = Sort.by(Sort.Direction.ASC, "dueDate").and(Sort.by("id"));
        return taskRepository.findAll(spec, sort);
    }

    public Page<Task> findByFiltersForRequesterPageable(User requester,
                                                        Task.Status status,
                                                        RequestType requestType,
                                                        Long assignedUser,
                                                        Pageable pageable) {
        return taskRepository.findByFiltersForRequesterPageable(
                requester, status, requestType, assignedUser, pageable);
    }
}
