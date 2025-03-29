package org.example.affaci.Models.DTO;


import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.affaci.Models.Enum.Role;
import org.example.affaci.Models.Enum.User_type;
import java.sql.Timestamp;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRegisterDTO {
    UUID id;
    String fio;
    String username;
    String password;
    User_type user_type;
    String google_id;
    Role role;
    String ip_address;
    String jwt_token;
    Timestamp login_time;
}
