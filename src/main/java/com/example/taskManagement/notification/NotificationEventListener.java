package com.example.taskManagement.notification;
import com.example.taskManagement.event.TaskAssignedUserChangedEvent;
import com.example.taskManagement.event.TaskCreatedEvent;
import com.example.taskManagement.event.TaskStatusChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onTaskCreated(TaskCreatedEvent event) {
        notificationService.sendTaskCreated(event.taskId());
    }

    @EventListener
    public void onTaskStatusChanged(TaskStatusChangedEvent event) {
        notificationService.sendTaskStatusChanged(event.taskId(), event.oldStatus(), event.newStatus());
    }
    @EventListener
    public void onTaskAssignedUserChangedEvent(TaskAssignedUserChangedEvent event) {
        notificationService.sendTaskAssignedUserChangedEvent(event.taskId(), event.newUser());
    }
}

