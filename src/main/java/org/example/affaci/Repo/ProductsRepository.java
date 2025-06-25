package org.example.affaci.Repo;


import org.example.affaci.Models.Entity.Categories;
import org.example.affaci.Models.Entity.Products;

import org.example.affaci.Models.Entity.Regions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductsRepository
        extends JpaRepository<Products, UUID>,
        JpaSpecificationExecutor<Products> {


    void deleteAllByNationalTrue();

    List<Products> findByNameContainingIgnoreCase(String name);

    List<Products> findAllByRegionId(UUID id);
    List<Products> findAllByCategoriesId(UUID id);

    Page<Products> findAll(Pageable pageable);



    @Query("select distinct p from Products p " +
            "left join fetch p.aminoAcidCompositions " +
            "left join fetch p.mineralCompositions " +
            "left join fetch p.chemicalCompositions " +
            "left join fetch p.fattyAcidCompositions " +
            "where p.id = :id")
    Optional<Products> findByIdWithDetails(@Param("id") UUID id);

    List<Products> findAllByNationalIsTrue();

    //Количество национальных блюд
    long countByNationalTrue();


    Optional<Products> findFirstByNameAndRegionAndCategories(String rusName, Regions region, Categories category);

    Optional<Products> findByNameAndRegionAndCategories(String productName, Regions region, Categories category);
}
