package com.example.taskManagement.service;


import com.example.taskManagement.entity.ChangeRequest;
import com.example.taskManagement.entity.Task;
import com.example.taskManagement.entity.User;
import com.example.taskManagement.repository.ChangeRequestRepository;
import com.example.taskManagement.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ChangeRequestService {

    private final ChangeRequestRepository changeRequestRepository;
    private final TaskRepository taskRepo;
    private final UserService userService;

    public ChangeRequestService(ChangeRequestRepository changeRequestRepository, TaskRepository taskRepo, UserService userService) {
        this.changeRequestRepository = changeRequestRepository;
        this.taskRepo = taskRepo;
        this.userService = userService;
    }

    public List<ChangeRequest> getAllChangeRequests() {
        return changeRequestRepository.findAll();
    }

    public List<Task> getCandidateTasks() {
        // show only TODO / IN_PROGRESS tasks (typical for CRs)
        return taskRepo.findByStatusIn(List.of(Task.Status.TODO, Task.Status.IN_PROGRESS));
    }

    @Transactional
    public ChangeRequest create(ChangeRequest cr, Long taskId) {
        if (taskId != null) {
            Task task = taskRepo.findById(taskId)
                    .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
            cr.setTask(task);
        } else {
            cr.setTask(null);
        }
        cr.setReviewedBy(null);
        if (cr.getStatus() == null) {
            cr.setStatus(ChangeRequest.Status.DRAFT);
        }
        return changeRequestRepository.save(cr);
    }
    @Transactional
    public ChangeRequest updateChangeRequest(Long id, ChangeRequest dto, Long taskId) {
        ChangeRequest cr = changeRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ChangeRequest not found: " + id));

        cr.setTitle(dto.getTitle());
        cr.setDescription(dto.getDescription());
        cr.setImpactAnalysis(dto.getImpactAnalysis());
        cr.setRisk(dto.getRisk());
        cr.setAffectedItem(dto.getAffectedItem());
        cr.setChangeReason(dto.getChangeReason());
        cr.setBackoutPlan(dto.getBackoutPlan());
        cr.setVerificationPlan(dto.getVerificationPlan());

        // You may or may not allow status changes via this form:
        if (dto.getStatus() != null) {
            cr.setStatus(dto.getStatus());
        }

        if (taskId != null) {
            Task task = taskRepo.findById(taskId)
                    .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));
            cr.setTask(task);
        } else {
            cr.setTask(null);
        }

        return changeRequestRepository.save(cr);
    }
    public Optional<ChangeRequest> findById(Long id) {
        return changeRequestRepository.findById(id);
    }
    // --- Submit for review (IT_TEAM / IT_MANAGER) ---
    @Transactional
    public ChangeRequest submitForReview(Long id, User submitter) {
        ChangeRequest cr = changeRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ChangeRequest not found: " + id));

        if (cr.getStatus() != ChangeRequest.Status.DRAFT) {
            throw new IllegalStateException("Only DRAFT requests can be submitted.");
        }

        cr.setStatus(ChangeRequest.Status.UNDER_REVIEW);
        cr.setSubmittedBy(submitter);
        cr.setSubmittedAt(java.time.LocalDateTime.now());
        cr.setReviewComment(null);
        cr.setReviewedBy(null);
        cr.setReviewedAt(null);

        return changeRequestRepository.save(cr);
    }

    // --- Approve (IT_DIRECTOR) ---
    @Transactional
    public ChangeRequest approve(Long id, User director, String comment) {
        ChangeRequest cr = changeRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ChangeRequest not found: " + id));

        if (cr.getStatus() != ChangeRequest.Status.UNDER_REVIEW) {
            throw new IllegalStateException("Only requests under review can be approved.");
        }

        cr.setStatus(ChangeRequest.Status.APPROVED);
        cr.setReviewedBy(director);
        cr.setReviewedAt(java.time.LocalDateTime.now());
        cr.setReviewComment(comment);

        return changeRequestRepository.save(cr);
    }

    // --- Decline (IT_DIRECTOR) ---
    @Transactional
    public ChangeRequest decline(Long id, User director, String comment) {
        ChangeRequest cr = changeRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("ChangeRequest not found: " + id));

        if (cr.getStatus() != ChangeRequest.Status.UNDER_REVIEW) {
            throw new IllegalStateException("Only requests under review can be declined.");
        }

        cr.setStatus(ChangeRequest.Status.REJECTED);
        cr.setReviewedBy(director);
        cr.setReviewedAt(java.time.LocalDateTime.now());
        cr.setReviewComment(comment);

        return changeRequestRepository.save(cr);
    }
    }
