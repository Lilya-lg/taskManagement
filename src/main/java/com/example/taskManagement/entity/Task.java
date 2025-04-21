package com.example.taskManagement.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @Column(name = "history", columnDefinition = "TEXT") // Store JSON as text
    private String history; // JSON-formatted history

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status cannot be null")
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column
    private Priority priority;

    @Column(name = "needApproval")
    private Boolean needApproval;

    @Column(name = "approved")
    private Boolean approved;

    public enum Status {
        TODO, IN_PROGRESS, COMPLETED
    }

    public enum Priority {
        Low, High, Medium
    }


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


    public void addHistory(String text) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode historyNode = history != null ? objectMapper.readTree(history) : objectMapper.createArrayNode();

            JsonNode newEntry = objectMapper.createObjectNode().put("date", LocalDateTime.now(ZoneId.systemDefault()).toString()).put("text", text);

            ((com.fasterxml.jackson.databind.node.ArrayNode) historyNode).add(newEntry);
            this.history = objectMapper.writeValueAsString(historyNode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

}