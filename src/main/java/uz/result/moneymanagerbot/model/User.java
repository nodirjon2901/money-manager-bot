package uz.result.moneymanagerbot.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "users")
public class User {

    @Id
    @GeneratedValue
    Integer id;

    Long chatId;

    String name;

    String password;

    @Enumerated(EnumType.STRING)
    UserState state;

    @Enumerated(EnumType.STRING)
    UserRole role;

    boolean signIn;

}
