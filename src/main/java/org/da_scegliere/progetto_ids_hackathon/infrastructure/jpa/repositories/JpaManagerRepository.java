package org.da_scegliere.progetto_ids_hackathon.infrastructure.jpa.repositories;

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IManagerRepository;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaManagerRepository extends JpaRepository<Manager, UUID>, IManagerRepository {
}
