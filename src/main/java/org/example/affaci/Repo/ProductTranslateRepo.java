package org.example.affaci.Repo;

import org.example.affaci.Models.Entity.Products_translate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface ProductTranslateRepo extends JpaRepository<Products_translate, UUID> {
}
