package org.example.affaci.Models.Entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Research_sources {

    @Id
    @UuidGenerator
    UUID id;

    String title; //название или заголовок исследования
    String author; //автор(ы) исследования
    LocalDate publication_date; //Дата публикации
    String url; //ссылка на документ или DOI (опционально)
    String description; //описание исследования (опционально)

    @OneToMany(mappedBy = "research_sources", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Product_research_sources> productResearchSources = new ArrayList<>();
}
