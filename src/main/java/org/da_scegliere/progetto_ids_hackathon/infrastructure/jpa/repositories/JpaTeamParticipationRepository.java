package org.da_scegliere.progetto_ids_hackathon.infrastructure.jpa.repositories;

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.ITeamParticipationRepository;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.TeamParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaTeamParticipationRepository extends JpaRepository<TeamParticipation, UUID>, ITeamParticipationRepository {
}
