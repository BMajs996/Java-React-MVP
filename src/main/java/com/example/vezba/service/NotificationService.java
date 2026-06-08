package com.example.vezba.service;

import com.example.vezba.model.AppUser;
import com.example.vezba.model.Notification;
import com.example.vezba.repository.NotificationRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationService {
    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    public List<Notification> list(AppUser recipient) {
        return notifications.findByRecipientOrderByCreatedAtDesc(recipient);
    }

    public Notification create(AppUser recipient, String message) {
        return notifications.save(new Notification(recipient, message));
    }

    public Notification markAsRead(AppUser recipient, Long notificationId) {
        Notification notification = notifications.findById(notificationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!notification.getRecipient().getId().equals(recipient.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update another user's notification");
        }
        notification.setRead(true);
        return notifications.save(notification);
    }
}
