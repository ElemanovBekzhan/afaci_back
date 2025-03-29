package org.example.affaci.Models.Entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Product_research_sources {

    @Id
    @UuidGenerator
    UUID id;


    @ManyToOne
    @JoinColumn(name = "product_id")
    Products product;

    @ManyToOne
    @JoinColumn(name ="research_sources_id")
    Research_sources research_sources;
}

