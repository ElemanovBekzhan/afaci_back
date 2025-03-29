package org.example.affaci.Models.Entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Organizations {

    @Id
    @UuidGenerator
    UUID id;
    @Column(unique = true, nullable = false)
    String name;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    User admin_user_id;
    Timestamp created_at;
    Timestamp updated_at;
    String address;



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
