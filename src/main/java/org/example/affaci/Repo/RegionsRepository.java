package org.example.affaci.Repo;


import org.example.affaci.Models.DTO.RegionListDTO;
import org.example.affaci.Models.Entity.Regions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RegionsRepository extends JpaRepository<Regions, UUID> {



    @Query("select distinct new org.example.affaci.Models.DTO.RegionListDTO(r.id, r.name) from Regions r order by r.name")
    List<RegionListDTO> findAllIdAndName();

    Optional<Regions> findByNameIgnoreCase(String name);
}
