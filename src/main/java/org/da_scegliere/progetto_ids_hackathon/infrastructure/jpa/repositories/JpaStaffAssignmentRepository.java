package org.da_scegliere.progetto_ids_hackathon.infrastructure.jpa.repositories;

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IStaffAssignmentRepository;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaStaffAssignmentRepository extends JpaRepository<StaffAssignment, UUID>, IStaffAssignmentRepository {
}
