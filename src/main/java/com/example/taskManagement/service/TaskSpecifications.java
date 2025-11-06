package com.example.taskManagement.service;

import com.example.taskManagement.entity.RequestType;
import com.example.taskManagement.entity.Task;
import com.example.taskManagement.entity.User;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;

public final class TaskSpecifications {

    private TaskSpecifications() {}

    public static Specification<Task> requesterIs(User requester) {
        return (root, q, cb) ->
                requester == null ? cb.conjunction() : cb.equal(root.get("requester"), requester);
    }

    public static Specification<Task> statusIs(Task.Status status) {
        return (root, q, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> requestTypeIs(RequestType type) {
        return (root, q, cb) ->
                type == null ? cb.conjunction() : cb.equal(root.get("requestType"), type);
    }

    public static Specification<Task> assignedUserIdIs(Long userId) {
        return (root, q, cb) -> {
            if (userId == null) return cb.conjunction();
            // join assignedUser and compare by id (works even if assignedUser is LAZY)
            var assignedUser = root.join("assignedUser", JoinType.LEFT);
            return cb.equal(assignedUser.get("id"), userId);
        };
    }
    public static Specification<Task> statusNot(Task.Status excludedStatus) {
        return (root, query, cb) -> cb.notEqual(root.get("status"), excludedStatus);
    }
}
