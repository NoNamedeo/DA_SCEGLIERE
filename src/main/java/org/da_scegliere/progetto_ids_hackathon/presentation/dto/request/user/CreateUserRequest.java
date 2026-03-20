package org.da_scegliere.progetto_ids_hackathon.presentation.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateUserRequest(
        @NotBlank @Size(min = 2, max = 50) String name,
        @Min(18) @Max(120) int age,
        @NotBlank @Email String email,
        @NotNull UUID teamId
) {
}
