package org.da_scegliere.progetto_ids_hackathon.infrastructure.jpa.repositories;

import jakarta.persistence.LockModeType;
import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IHackathonRepository;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaHackathonRepository extends JpaRepository<Hackathon, UUID>, IHackathonRepository {

    @Override
    @Query("select h.id from Hackathon h")
    List<UUID> findAllIds();

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Hackathon h where h.id = :hackathonId")
    Optional<Hackathon> findByIdForUpdate(@Param("hackathonId") UUID hackathonId);
}
