package org.da_scegliere.progetto_ids_hackathon.infrastructure.jpa.repositories;

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IHackathonRepository;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaHackathonRepository extends JpaRepository<Hackathon, UUID>, IHackathonRepository {
}
