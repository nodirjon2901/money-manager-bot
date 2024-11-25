package uz.result.moneymanagerbot.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    Long chatId;

    @Column(length = 2000)
    String message;

    Double summa;

    @Enumerated(EnumType.STRING)
    TransactionType type;

    LocalDateTime time;

    @Enumerated(EnumType.STRING)
    RepeatPeriod repeatInterval;

}
