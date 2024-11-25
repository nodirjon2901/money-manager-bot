package uz.result.moneymanagerbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.result.moneymanagerbot.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

}
