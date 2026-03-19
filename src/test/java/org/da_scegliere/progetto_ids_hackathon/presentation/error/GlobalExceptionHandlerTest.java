package org.da_scegliere.progetto_ids_hackathon.presentation.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.calendar.CalendarUnavailableException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.team.TeamNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private static final String TEAM_ID = "00000000-0000-0000-0000-000000000001";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void shouldReturnNotFoundForDomainNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.RESOURCE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value("Team with id '" + TEAM_ID + "' was not found."))
                .andExpect(jsonPath("$.path").value("/test/not-found"))
                .andExpect(jsonPath("$.violations").isArray());
    }

    @Test
    void shouldReturnValidationDetailsForInvalidRequestBody() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.violations[*].field", containsInAnyOrder("name", "age")));
    }

    @Test
    void shouldReturnMalformedRequestForInvalidJson() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.MALFORMED_REQUEST.name()))
                .andExpect(jsonPath("$.message").value("Request body is malformed or contains invalid values."));
    }

    @Test
    void shouldReturnServiceUnavailableWhenCalendarProviderIsUnavailable() throws Exception {
        mockMvc.perform(get("/test/calendar-unavailable"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.UPSTREAM_SERVICE_UNAVAILABLE.name()))
                .andExpect(jsonPath("$.message").value("Calendar provider is unavailable."));
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedExceptions() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value(ApiErrorCode.INTERNAL_SERVER_ERROR.name()))
                .andExpect(jsonPath("$.message").value(
                        "An internal error occurred. Please contact support if the problem persists."
                ));
    }

    @RestController
    @RequestMapping("/test")
    private static class TestController {

        @GetMapping("/not-found")
        void notFound() {
            throw new TeamNotFoundException(UUID.fromString(TEAM_ID));
        }

        @PostMapping("/validation")
        void validation(@Valid @RequestBody ValidationRequest request) {
            // No-op endpoint used to trigger validation errors in tests.
        }

        @GetMapping("/calendar-unavailable")
        void calendarUnavailable() {
            throw new CalendarUnavailableException("Calendar provider is unavailable.");
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("Unexpected state");
        }
    }

    private record ValidationRequest(
            @NotBlank String name,
            @NotNull Integer age
    ) {
    }
}
