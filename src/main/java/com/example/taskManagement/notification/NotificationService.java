package com.example.taskManagement.notification;

import com.example.taskManagement.entity.User;

public interface NotificationService {
    void sendTaskCreated(Long taskId);
    void sendTaskStatusChanged(Long taskId, String oldStatus, String newStatus);
    void sendTaskAssignedUserChangedEvent(Long taskId, User newUser);
}

