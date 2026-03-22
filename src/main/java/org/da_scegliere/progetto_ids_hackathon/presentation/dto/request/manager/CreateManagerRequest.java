package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.manager;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateManagerRequest(
        @NotBlank @Size(min = 2, max = 50) String name,
        @Min(18) @Max(120) int age,
        @NotBlank @Email String email
) {
}
