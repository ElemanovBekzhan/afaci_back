package org.example.affaci.Repo;


import org.example.affaci.Models.Entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoriesRepository extends JpaRepository<Categories, UUID> {

    @Query("select c from Categories c where c.name_of_category = :name")
    Categories findByName(@Param("name") String categoryName);
}
