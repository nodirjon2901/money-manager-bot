package uz.result.moneymanagerbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.result.moneymanagerbot.exceptions.NotFoundException;
import uz.result.moneymanagerbot.model.Notification;
import uz.result.moneymanagerbot.repository.NotificationRepository;

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

}
