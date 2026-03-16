package org.da_scegliere.progetto_ids_hackathon.application.services;

import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.CalendarStrategy;
import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.exceptions.CalendarProviderUnavailableException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.calendar.CalendarConflictException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.calendar.CalendarUnavailableException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.entities.support.SupportRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private CalendarStrategy calendarStrategy;

    private CalendarService calendarService;

    @BeforeEach
    void setUp() {
        calendarService = new CalendarService(calendarStrategy);
    }

    @Test
    void proposeCallWithNullRequestThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> calendarService.proposeCall(null));
        verifyNoInteractions(calendarStrategy);
    }

    @Test
    void proposeCallWithPastDateThrowsException() {
        SupportRequest request = new SupportRequest(
                LocalDate.of(2026, 3, 13),
                new Team(),
                List.of()
        );

        assertThrows(IllegalArgumentException.class, () -> calendarService.proposeCall(request));
        verifyNoInteractions(calendarStrategy);
    }

    @Test
    void proposeCallWhenSlotIsOccupiedThrowsConflictException() {
        SupportRequest request = new SupportRequest(
                LocalDate.of(2026, 3, 14),
                new Team(),
                List.of()
        );

        when(calendarStrategy.isSlotAvailable(request)).thenReturn(false);

        assertThrows(CalendarConflictException.class, () -> calendarService.proposeCall(request));
        verify(calendarStrategy, never()).reserveCallSlot(request);
    }

    @Test
    void isSlotAvailableWhenProviderIsDownThrowsUnavailableException() {
        SupportRequest request = new SupportRequest(
                LocalDate.of(2026, 3, 14),
                new Team(),
                List.of()
        );

        when(calendarStrategy.isSlotAvailable(request))
                .thenThrow(new CalendarProviderUnavailableException("Provider down"));

        assertThrows(CalendarUnavailableException.class, () -> calendarService.isSlotAvailable(request));
    }

    @Test
    void isSlotAvailableReturnsProviderAvailability() {
        SupportRequest request = new SupportRequest(
                LocalDate.of(2026, 3, 14),
                new Team(),
                List.of()
        );

        when(calendarStrategy.isSlotAvailable(request)).thenReturn(false);

        assertFalse(calendarService.isSlotAvailable(request));
    }
}
