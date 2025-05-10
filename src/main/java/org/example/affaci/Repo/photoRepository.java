package org.example.affaci.Repo;

import org.example.affaci.Models.Entity.photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;


@Repository
public interface photoRepository extends JpaRepository<photo, UUID> {
}
