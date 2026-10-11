package org.example.ecommerc_shop.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ecommerc_shop.common.NotificationStatus;
import org.example.ecommerc_shop.entity.Notification;
import org.example.ecommerc_shop.repository.NotificationRepository;
import org.example.ecommerc_shop.service.EmailService;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void sendPendingNotifications() {
        List<Notification> notifications = notificationRepository.findByStatusOrderByCreatedAtAsc(NotificationStatus.PENDING, PageRequest.of(0, 50));
        for (Notification notification : notifications) {
            try {
                String email = notification.getUser().getEmail();
                emailService.sendOrderNotification(
                        email,
                        notification.getTitle(),
                        notification.getContent()
                );
                notification.setStatus(NotificationStatus.SENT);
                notification.setErrorMessage(null);
            } catch (Exception e) {
                notification.setStatus(NotificationStatus.FAILED);
                notification.setErrorMessage(e.getMessage());

                log.error(
                        "Failed to send notification: {}",
                        notification.getId(),
                        e
                );
            }
            notificationRepository.save(notification);
        }
    }
}
