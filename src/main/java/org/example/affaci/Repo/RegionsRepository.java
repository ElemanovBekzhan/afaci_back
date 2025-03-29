package org.example.affaci.Repo;


import org.example.affaci.Models.Entity.Regions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RegionsRepository extends JpaRepository<Regions, UUID> {
    Regions findByName(String regionName);
}
