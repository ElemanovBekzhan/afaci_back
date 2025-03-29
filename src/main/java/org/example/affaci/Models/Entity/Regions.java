package org.example.affaci.Models.Entity;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;


import java.sql.Timestamp;
import java.util.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class Regions {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Regions parent;   //Родительский элемент, ссылка на id другого объека(страны допустим) если NULL Значит страна


    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<Regions> children = new HashSet<>();  //Коллекция подрегионов текущего региона

    String code; //Кода региона
    String name;  //Наименование региона или страны
    Integer level; //Уровень иерархии (например, 0 для страны, 1 для областей/регионов)

    Timestamp created_at;
    Timestamp updated_at;


    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Categories> categories = new ArrayList<>();



    @PrePersist
    protected void onCreate() {
        created_at = new Timestamp(System.currentTimeMillis());
        updated_at = created_at;
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = new Timestamp(System.currentTimeMillis());
    }


}
