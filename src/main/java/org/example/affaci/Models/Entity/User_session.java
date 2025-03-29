package org.example.affaci.Models.Entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.affaci.Models.DTO.UserRegisterDTO;
import org.hibernate.annotations.UuidGenerator;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Setter
public class User_session {

    @Id
    @UuidGenerator
    UUID id;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    User user;
    Timestamp login_time;
    Timestamp logout_time;
    String ip_address;
    String jwt_token;



}