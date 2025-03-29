package org.example.affaci.Models.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.affaci.Models.DTO.UserRegisterDTO;
import org.example.affaci.Models.Enum.Position_in_org;
import org.example.affaci.Models.Enum.Role;
import org.example.affaci.Models.Enum.User_type;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "users")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @Column(nullable = false, unique = true)
    String username;
    @Column(nullable = false)
    String fio;
    @Column(nullable = false)
    String password;
    @Enumerated(EnumType.STRING)
    User_type user_type;
    @Column(unique = true)
    String google_id;
    Integer failed_login_attempts; //Количество неудачных попыток входа (для ограничения)
    Timestamp last_failed_login; //Время последней неудачной попытки (для отслеживания интервала)
    Timestamp created_at;
    Timestamp updated_at;
    @Enumerated(EnumType.STRING)
    Role role; //Роль пользователя (Обычный/Администратор)
    @Enumerated(EnumType.STRING)
    Position_in_org position_in_org; //Должность в организации, если привязан к организации


    @PrePersist
    protected void onCreate() {
        created_at = new Timestamp(System.currentTimeMillis());
        updated_at = created_at;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = new Timestamp(System.currentTimeMillis());
    }


}