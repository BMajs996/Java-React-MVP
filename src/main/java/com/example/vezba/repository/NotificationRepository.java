package com.example.vezba.repository;

import com.example.vezba.model.AppUser;
import com.example.vezba.model.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(AppUser recipient);
}
