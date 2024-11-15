package uz.result.moneymanagerbot.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    String systemPath;

    String httpUrl;

    String type;

    public FileEntity(String name, String systemPath, String httpUrl, String type) {
        this.name = name;
        this.systemPath = systemPath;
        this.httpUrl = httpUrl;
        this.type = type;
    }

    @OneToOne(mappedBy = "file")
    Transaction transaction;
}

