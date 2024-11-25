package uz.result.moneymanagerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.result.moneymanagerbot.exceptions.NotFoundException;
import uz.result.moneymanagerbot.model.Notification;
import uz.result.moneymanagerbot.model.RepeatPeriod;
import uz.result.moneymanagerbot.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    public Notification findById(Integer id) {
        return notificationRepository.findById(id).orElseThrow(() -> new NotFoundException("Notification is not found with id: " + id));
    }

    public void deleteById(Integer id) {
        notificationRepository.deleteById(id);
    }

    public void updateNotificationTimeById(LocalDateTime time, Integer id) {
        notificationRepository.updateNotificationTimeById(time, id);
    }

    public void updateNotificationTextById(Integer id, String text) {
        notificationRepository.updateNotificationTextById(text, id);
    }

    public void updateNotificationSummaById(Integer id, Double summa) {
        notificationRepository.updateNotificationSummaById(summa, id);
    }

    public void updateNotificationTransactionTypeById(String type, Integer id) {
        notificationRepository.updateNotificationTransactionTypeById(type, id);
    }

    public void updateNotificationTimeRepeatById(String data, Integer id) {
        notificationRepository.updateNotificationTimeRepeatById(data, id);
    }

    public List<Notification> processNotifications() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC")).plusHours(5)
                .withSecond(0)
                .withNano(0);
        List<Notification> notifications = notificationRepository.findByTime(now);
        List<Notification> notificationList = new ArrayList<>();
        for (Notification notification : notifications) {
            notificationList.add(notification);
            if (notification.getRepeatInterval() == RepeatPeriod.ONCE) {
                notificationRepository.delete(notification);
            } else {
                notification.setTime(calculateNextTime(notification));
                notificationRepository.save(notification);
            }
        }
        return notificationList;
    }

    private LocalDateTime calculateNextTime(Notification notification) {
        switch (notification.getRepeatInterval()) {
            case WEEKLY:
                return notification.getTime().plusWeeks(1);
            case MONTHLY:
                return notification.getTime().plusMonths(1);
            case QUARTERLY:
                return notification.getTime().plusMonths(3);
            default:
                throw new IllegalArgumentException("Invalid RepeatPeriod");
        }
    }

}
