package org.example.affaci.Repo;


import org.example.affaci.Models.DTO.CategoryListDTO;
import org.example.affaci.Models.Entity.Categories;
import org.example.affaci.Models.Entity.Regions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, UUID> {

    @Query("select c from Categories c where c.name = :name")
    Categories findByName(@Param("name") String categoryName);

    @Query("select distinct new org.example.affaci.Models.DTO.CategoryListDTO(MIN(c.id), c.name) from Categories c group by c" +
            ".name order by c.name")
    List<CategoryListDTO> findAllIdAndName();


    Optional<Categories> findByNameAndRegion(String nameOfCategory, Regions region);
}
