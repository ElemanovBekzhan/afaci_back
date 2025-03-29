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
public class Vitamin_composition {

    @Id
    @UuidGenerator
    UUID id;

    @OneToOne
    @JoinColumn(name = "products_id")
    Products product;


    String vitamit_name;            //название витамина (например, Vitamin A, Vitamin C)
    String vitamit_group;           //группа витаминов (например, жирорастворимые, водорастворимые)
    Double quantity;                //Количество
    Double error;                   //Погрешность
    @Enumerated(EnumType.STRING)
    Unit unit;                      //Единица измерения
}
