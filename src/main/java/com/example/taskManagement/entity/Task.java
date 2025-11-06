package com.example.taskManagement.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;


@Entity
@Table(name = "tasks")
@Data
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Title cannot be null")
    @Column(nullable = false)
    private String title;

    @NotNull(message = "Description cannot be null")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "history", columnDefinition = "TEXT")
    private String history;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status cannot be null")
    @Column(nullable = false)
    private Status status = Status.TODO;

    @Enumerated(EnumType.STRING)
    @Column
    private Priority priority;

    @Column(name = "needApproval")
    private Boolean needApproval;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private java.util.List<TaskComment> comments = new java.util.ArrayList<>();

    @Column(name = "approved")
    private Boolean approved;

    @Column(unique = true)
    private String externalId;

    public enum Status {
        TODO, IN_PROGRESS, COMPLETED, DELETED
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

    @ManyToOne
    @JoinColumn(name = "requester_id")
    private User requester;

    @Column(name = "file_name", length = 255)
    private String fileName;
    @Column(name = "file_path", length = 255)
    private String filePath;

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