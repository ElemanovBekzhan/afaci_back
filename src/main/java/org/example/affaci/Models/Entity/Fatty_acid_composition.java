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
public class Fatty_acid_composition {

    @Id
    @UuidGenerator
    UUID id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    Products product;

    String fatty_acid_name;     //Название жирной кислоты
    String type_of_fatty_acid;  //Тип жирной кислоты
    Double quantity;            //Количество жирной кислоты (например, г на 100г продукта)
    Double error;               //Погрешность
    @Enumerated(EnumType.STRING)
    @Column(name = "unit", columnDefinition = "VARCHAR(255)")
    Unit unit;                  //Единица измерения
}
