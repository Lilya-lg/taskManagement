package com.example.taskManagement.event;

import com.example.taskManagement.entity.User;

public record TaskAssignedUserChangedEvent(Long taskId, User newUser) {
}
