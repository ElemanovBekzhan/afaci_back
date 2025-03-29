package org.example.affaci.Models.Entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.affaci.Models.Enum.Language;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Products_translate {

    @Id
    @UuidGenerator
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    Products products;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    Language language;
    @Column(nullable = false)
    String product_name;
    @Column(nullable = false)
    String description;
}
