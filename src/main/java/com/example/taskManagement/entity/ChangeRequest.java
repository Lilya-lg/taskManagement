package com.example.taskManagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "change_requests")  // Mapping to the 'change_requests' table
@Data
public class ChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Title cannot be null")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Column(nullable = false)
    private String title;

    @NotNull(message = "Description cannot be null")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "impact_analysis", columnDefinition = "TEXT")
    private String impactAnalysis;

    @Column(name = "risk", columnDefinition = "TEXT")
    private String risk;

    @Column(name = "backout_plan", columnDefinition = "TEXT")
    private String backoutPlan;

    @Column(name = "verification_plan", columnDefinition = "TEXT")
    private String verificationPlan;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status cannot be null")
    @Column(nullable = false)
    private Status status;

    public enum Status {
        DRAFT, UNDER_REVIEW, APPROVED, REJECTED
    }

    // Mapping to Task (foreign key to 'tasks' table)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    // Mapping to User (foreign key to 'users' table, for the person approving the request)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", referencedColumnName = "id", nullable = true)
    private User approvedBy;
}