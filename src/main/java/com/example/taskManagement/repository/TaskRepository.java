package com.example.taskManagement.repository;

import com.example.taskManagement.entity.RequestType;
import com.example.taskManagement.entity.Task;
import com.example.taskManagement.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>,
        org.springframework.data.jpa.repository.JpaSpecificationExecutor<Task> {
    // Method to count tasks by status
    long countByStatus(Task.Status status);

    long countByAssignedUser(User user);

    long countByStatusAndRequestType(Task.Status status, RequestType request_type);

    long countByRequestType(RequestType request_type);

    long countByRequester(User user);

    long countByStatusAndRequester(Task.Status status, User user);

    long countByStatusAndAssignedUser(Task.Status status, User user);

    List<Task> findByStatusNotInAndRequestType(List<Task.Status> statuses, RequestType requestType);

    long countByDueDateBeforeAndStatusNotAndAssignedUser(LocalDate date, Task.Status status, User user);

    // Method to count overdue tasks that are not completed
    long countByDueDateBeforeAndStatusNot(LocalDate date, Task.Status status);

    long countByDueDateBeforeAndStatusNotAndRequester(LocalDate date, Task.Status status, User user);

    List<Task> findByStatusAndRequestType(Task.Status status, RequestType requestType);

    List<Task> findByRequester(User user);

    List<Task> findByAssignedUser(User user);

    List<Task> findByAssignedUserOrRequester(User user,User user2);

    Optional<Task> findByExternalId(String externalId);

    long countByDueDateBeforeAndStatusNotAndRequestType(LocalDate date, Task.Status status, RequestType request_type);
    List<Task> findByStatusIn(Collection<Task.Status> statuses);

    @Query("""
            SELECT t FROM Task t 
            WHERE t.status <> 'DELETED'
              AND (:requester IS NULL OR t.requester = :requester)
              AND (:status IS NULL OR t.status = :status)
              AND (:requestType IS NULL OR t.requestType = :requestType)
              AND (:assignedUser IS NULL OR t.assignedUser.id = :assignedUser)
            ORDER BY t.dueDate DESC
            """)
    Page<Task> findByFiltersForRequesterPageable(@Param("requester") User requester,
                                                 @Param("status") Task.Status status,
                                                 @Param("requestType") RequestType requestType,
                                                 @Param("assignedUser") Long assignedUser,
                                                 Pageable pageable);

    @Query("""
       SELECT t FROM Task t
       WHERE (t.requester = :user OR t.assignedUser = :user)
       AND t.status <> 'CLOSED'
       AND t.status <> 'DELETED'
       ORDER BY t.dueDate ASC, t.id ASC
       """)
    List<Task> findActiveTasksForUser(@Param("user") User user);

}

