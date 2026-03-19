package org.da_scegliere.progetto_ids_hackathon.infrastructure.jpa.repositories;

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffReportRepository;
import org.da_scegliere.progetto_ids_hackathon.core.entities.moderation.StaffReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaStaffReportRepository extends JpaRepository<StaffReport, UUID>, IStaffReportRepository {
}
