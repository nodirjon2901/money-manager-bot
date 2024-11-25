package uz.result.moneymanagerbot.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.result.moneymanagerbot.model.Notification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    @Modifying
    @Transactional
    @Query(value = "update notification set time=:time where id=:id", nativeQuery = true)
    void updateNotificationTimeById(@Param("time") LocalDateTime time, @Param("id") Integer id);


    @Modifying
    @Transactional
    @Query(value = "update notification set message=:text where id=:id", nativeQuery = true)
    void updateNotificationTextById(@Param("text") String text, @Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "update notification set summa=:summa where id=:id", nativeQuery = true)
    void updateNotificationSummaById(@Param("summa") Double summa, @Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "update notification set type=:type where id=:id", nativeQuery = true)
    void updateNotificationTransactionTypeById(@Param("type") String type, @Param("id") Integer id);

    @Modifying
    @Transactional
    @Query(value = "update notification set repeat_interval=:repeat where id=:id", nativeQuery = true)
    void updateNotificationTimeRepeatById(@Param("repeat") String data, @Param("id") Integer id);

    List<Notification> findByTime(LocalDateTime time);

}
