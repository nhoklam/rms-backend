package com.company.rms.service.general;

import com.company.rms.entity.general.Notification;
import com.company.rms.repository.general.NotificationRepository;
import com.company.rms.security.SecurityUtils;
import com.company.rms.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(Long recipientId, String title, String message, String type) {
        Notification notif = Notification.builder()
                .recipientId(recipientId)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notif);
    }

    @Transactional(readOnly = true)
    public List<Notification> getMyNotifications() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUserId);
    }

    @Transactional
    public void markAsRead(Long id) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (notif.getRecipientId().equals(currentUserId)) {
            notif.setIsRead(true);
            notificationRepository.save(notif);
        }
    }
    
    @Transactional
    public void markAllAsRead() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<Notification> list = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUserId);
        list.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(list);
    }
}