package com.example.taskManagement.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import java.util.Properties;

import org.jsoup.Jsoup;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class Pop3MailService{
    private final TaskService taskService;

    public Pop3MailService(TaskService taskService) {
        this.taskService = taskService;
    }
    @Scheduled(fixedDelay = 60000)
    public void fetchEmails() {
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "pop3s");
            props.put("mail.pop3s.ssl.enable", "true");
            props.put("mail.pop3s.ssl.trust", "*");
            props.put("mail.pop3s.ssl.checkserveridentity", "false");

            Session session = Session.getInstance(props);
            Store store = session.getStore("pop3s");
            store.connect("1.1.1.1", 995, "i", "1");

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            for (Message message : messages) {
                String subject = message.getSubject();
                String from = ((InternetAddress) message.getFrom()[0]).getAddress();
                String messageId = message.getHeader("Message-ID") != null
                        ? message.getHeader("Message-ID")[0]
                        : null;

                String body = "";
                Object content = message.getContent();
                if (content instanceof String) {
                    body = (String) content;
                } else if (content instanceof Multipart) {
                    Multipart mp = (Multipart) content;
                    for (int i = 0; i < mp.getCount(); i++) {
                        BodyPart part = mp.getBodyPart(i);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            // обработка вложений по желанию
                        } else {
                            Object partContent = part.getContent();
                            if (partContent instanceof String) {
                                body += (String) partContent;
                            }
                        }
                    }
                }
                 body = extractText(message);
                taskService.createTaskFromEmail(subject, body, from, messageId);
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private String extractText(Part part) throws Exception {
        if (part.isMimeType("text/plain")) {
            return (String) part.getContent();
        } else if (part.isMimeType("text/html")) {
            String html = (String) part.getContent();
            return Jsoup.parse(html).text();
        } else if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String text = extractText(mp.getBodyPart(i));
                if (text != null && !text.isBlank()) {
                    return text;
                }
            }
        }
        return "";
    }
}
