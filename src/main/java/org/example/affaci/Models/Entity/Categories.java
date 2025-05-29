package org.example.affaci.Models.Entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Categories {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    String name;        //Наименование категории Мясные, (Молочные, Хлебобулочные и т.д.)

    @ManyToOne
    @JoinColumn(name = "regions_id")
    Regions region;






}
