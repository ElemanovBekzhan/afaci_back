package org.example.affaci.Models.Entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.affaci.Models.Enum.Unit;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Amino_acid_composition {   //Аминокислотный состав
    @Id
    @UuidGenerator
    UUID id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    Products product;            //Продукт
    String amino_acid_name;     //Название аминокислоты
    Double quantity;            //Количество
    Double error;               //Погрешность
    @Enumerated(EnumType.STRING)
    @Column(name = "unit", columnDefinition = "VARCHAR(255)")
    Unit unit;
}