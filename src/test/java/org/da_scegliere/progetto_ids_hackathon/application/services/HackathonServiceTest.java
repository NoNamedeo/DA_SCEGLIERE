package org.da_scegliere.progetto_ids_hackathon.application.services;

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.IHackathonRepository;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.HackathonNotFoundException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.hackathon.InvalidHackathonStateTransitionException;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffAssignment;
import org.da_scegliere.progetto_ids_hackathon.core.states.hackathon.HackathonState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HackathonServiceTest {

    @Mock
    private IHackathonRepository hackathonRepository;

    private HackathonService hackathonService;

    @BeforeEach
    void setUp() {
        hackathonService = new HackathonService(hackathonRepository);
    }

    @Test
    void getHackathonByIdWhenNotFoundThrowsException() {
        UUID id = UUID.randomUUID();
        when(hackathonRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(HackathonNotFoundException.class, () -> hackathonService.getHackathonById(id));
    }

    @Test
    void transitionHackathonStateWhenTransitionIsInvalidThrowsTypedException() {
        UUID id = UUID.randomUUID();
        Hackathon hackathon = mock(Hackathon.class);

        when(hackathonRepository.findById(id)).thenReturn(Optional.of(hackathon));
        when(hackathon.getHackathonState()).thenReturn(HackathonState.ENDED);
        doThrow(new IllegalStateException("Invalid transition"))
                .when(hackathon)
                .transitionTo(HackathonState.ONGOING);

        assertThrows(
                InvalidHackathonStateTransitionException.class,
                () -> hackathonService.transitionHackathonState(id, HackathonState.ONGOING)
        );
        verify(hackathonRepository, never()).save(any(Hackathon.class));
    }

    @Test
    void createHackathonPersistsNewAggregate() {
        List<StaffAssignment> staffAssignments = List.of(mock(StaffAssignment.class));
        when(hackathonRepository.save(any(Hackathon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Hackathon createdHackathon = hackathonService.createHackathon("Main event", List.of(), staffAssignments);

        assertEquals("Main event", createdHackathon.getDescription());
        assertEquals(HackathonState.REGISTRATION, createdHackathon.getHackathonState());
        verify(hackathonRepository).save(any(Hackathon.class));
    }

    @Test
    void existsByIdWithNullIdThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> hackathonService.existsById(null));
    }
}
