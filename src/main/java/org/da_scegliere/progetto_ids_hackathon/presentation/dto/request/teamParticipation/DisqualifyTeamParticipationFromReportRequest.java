package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.teamParticipation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DisqualifyTeamParticipationFromReportRequest(
        @NotBlank @Size(max = 500) String disqualificationReason,
        @NotBlank @Size(max = 500) String reportResolutionNotes
) {
}
