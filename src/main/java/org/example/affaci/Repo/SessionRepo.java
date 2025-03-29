package org.example.affaci.Repo;

import org.example.affaci.Models.Entity.User_session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SessionRepo extends JpaRepository<User_session, UUID> {
}
