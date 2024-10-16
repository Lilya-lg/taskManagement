package com.example.taskManagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "tasks")  // Mapping to the 'tasks' table
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Title cannot be null")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Column(nullable = false)
    private String title;

    @NotNull(message = "Description cannot be null")
    @Column(nullable = false, columnDefinition = "TEXT")  // To allow longer descriptions
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status cannot be null")
    @Column(nullable = false)
    private Status status;

    public enum Status {
        TODO, IN_PROGRESS, COMPLETED
    }

    @NotNull
    @Column(name = "due_date")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    // Mapping to the User entity (foreign key to the 'users' table)
    @ManyToOne(fetch = FetchType.LAZY)  // Lazy fetching for performance reasons
    @JoinColumn(name = "assigned_user_id", referencedColumnName = "id", nullable = true)
    private User assignedUser;

    // Requester field (the current logged-in user who creates the task)
    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;  // This will automatically store the current logged-in user

    @Column(name = "file_name", length = 255)
    private String fileName;  // Store the file name
    @Column(name = "file_path", length = 255)  // Explicitly set file path column
    private String filePath;  // Store the file path

    @Enumerated(EnumType.STRING)
    private RequestType requestType;
}