package com.example.taskManagement.notification;


import com.example.taskManagement.entity.User;
import com.example.taskManagement.repository.TaskRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    private final JavaMailSender mailSender;
    private final TaskRepository taskRepository;

    public EmailNotificationService(JavaMailSender mailSender, TaskRepository taskRepository) {
        this.mailSender = mailSender;
        this.taskRepository = taskRepository;
    }

    @Override
    @Async
    public void sendTaskCreated(Long taskId) {
        taskRepository.findById(taskId).ifPresent(task -> {
            String to = task.getRequester().getEmail();
            String subject = "Заявка зарегистрирована №" + task.getId();
            String copy = "iroda.madaminova@umpt.uz";
            String body = """
                    Здравствуйте!

                    Ваша заявка успешно зарегистрирована.
                    Номер: %d
                    Тема: %s
                    Статус: %s

                    С уважением,
                    IT Support
                    """.formatted(task.getId(), task.getTitle(), task.getStatus());
            sendText(to, subject, body,"iroda.madaminova@umpt.uz");
        });
    }

    @Override
    @Async
    public void sendTaskStatusChanged(Long taskId, String oldStatus, String newStatus) {
        taskRepository.findById(taskId).ifPresent(task -> {
            String to = task.getRequester().getEmail();
            String subject = "Заявка №" + task.getId() + " → статус: " + newStatus;
            String body = """
                    Здравствуйте!

                    Статус вашей заявки изменен.
                    Номер: %d
                    Тема: %s
                    Был: %s
                    Стал: %s

                    С уважением,
                    IT Support
                    """.formatted(task.getId(), task.getTitle(), oldStatus, newStatus);
            sendTextWithoutCopy(to, subject, body);
        });
    }

    @Override
    @Async
    public void sendTaskAssignedUserChangedEvent(Long taskId, User newUser) {
        taskRepository.findById(taskId).ifPresent(task -> {
            String to = newUser.getEmail().toString();
            String subject = "Заявка №" + task.getId() + " → статус: " + task.getStatus();
            String body = """
                    Здравствуйте!

                    Вам поступила новая заявка.
                    Номер: %d
                    Тема: %s
                    
                    С уважением,
                    IT Support
                    """.formatted(task.getId(), task.getTitle());
            sendTextWithoutCopy(to, subject, body);
        });
    }

    private void sendText(String to, String subject, String text, String copy) {
        try {
            MimeMessage mm = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mm, false, "UTF-8");
            helper.setTo(to);
            helper.setFrom("itsm@umpt.uz");
            helper.setSubject(subject);
            helper.addCc(copy);
            helper.setText(text, false);
            mailSender.send(mm);
        } catch (Exception e) {
            System.err.println("Email send error: " + e.getMessage());
        }
    }
    private void sendTextWithoutCopy(String to, String subject, String text) {
        try {
            MimeMessage mm = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mm, false, "UTF-8");
            helper.setTo(to);
            helper.setFrom("itsm@umpt.uz");
            helper.setSubject(subject);
            helper.setText(text, false);
            mailSender.send(mm);
        } catch (Exception e) {
            System.err.println("Email send error: " + e.getMessage());
        }
    }
}

