package org.da_scegliere.progetto_ids_hackathon.infrastructure.jpa.repositories;

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IBugReportRepository;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.BugReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaBugReportRepository extends JpaRepository<BugReport, UUID>, IBugReportRepository {
}
