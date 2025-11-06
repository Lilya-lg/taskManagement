package com.example.taskManagement.event;

public record TaskStatusChangedEvent(Long taskId, String oldStatus, String newStatus) {}

