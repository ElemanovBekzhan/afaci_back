package org.example.affaci.Models.DTO;


import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.affaci.Models.Entity.Categories;

import java.sql.Timestamp;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductsDTO {
    UUID id;
    String name;
    String categories;
    String region;
    Timestamp date;
}
