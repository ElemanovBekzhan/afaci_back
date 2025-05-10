package org.example.affaci.Models.DTO;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponseDTO {

    UUID id;
    String name;
    String categories;
    String region;
    Timestamp datе;
    List<String> photoUrls;

}
