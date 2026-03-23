package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.hackathon;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateHackathonRequest(
        @NotNull UUID creatorId,
        @NotBlank String name,
        @NotBlank String description,
        @NotNull @DecimalMin(value = "0.01") BigDecimal awardPrize,
        @FutureOrPresent LocalDate registrationDeadline,
        @FutureOrPresent LocalDate submissionDeadline,
        @FutureOrPresent LocalDate evaluationDeadline
) {
}
