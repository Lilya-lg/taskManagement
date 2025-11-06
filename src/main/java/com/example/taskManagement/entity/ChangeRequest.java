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

    @Column(name = "affected_item", columnDefinition = "TEXT")
    private String affectedItem;

    @Column(name = "changeReason", columnDefinition = "TEXT")
    private String changeReason;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status cannot be null")
    @Column(nullable = false)
    private Status status;

    public enum Status {
        DRAFT, UNDER_REVIEW, APPROVED, REJECTED
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_id")
    private User submittedBy;

    private java.time.LocalDateTime submittedAt;


    // Mapping to Task (foreign key to 'tasks' table)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    private java.time.LocalDateTime reviewedAt;

    @Column(columnDefinition = "TEXT")
    private String reviewComment; // director's approve/decline note

    // Convenience: whether the record is editable in UI/service
    @Transient
    public boolean isLocked() {
        // lock when under review or decided
        return status == Status.UNDER_REVIEW || status == Status.APPROVED || status == Status.REJECTED;
    }
}