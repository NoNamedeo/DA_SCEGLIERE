package org.da_scegliere.progetto_ids_hackathon.infrastructure.jpa.repositories;

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ISupportRequestRepository;
import org.da_scegliere.progetto_ids_hackathon.core.entities.support.SupportRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaSupportRequestRepository extends JpaRepository<SupportRequest, UUID>, ISupportRequestRepository {
}
