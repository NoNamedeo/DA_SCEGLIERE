package org.da_scegliere.progetto_ids_hackathon.application.services;

import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonCrudService;
import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonLifecycleService;
import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonStaffService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.enums.states.hackathon.HackathonState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HackathonServiceTest {

    @Mock
    private HackathonCrudService hackathonCrudService;

    @Mock
    private HackathonLifecycleService hackathonLifecycleService;

    @Mock
    private HackathonStaffService hackathonStaffService;

    private HackathonService hackathonService;

    @BeforeEach
    void setUp() {
        hackathonService = new HackathonService(hackathonCrudService, hackathonLifecycleService, hackathonStaffService);
    }

    @Test
    void getHackathonByIdDelegatesToCrudService() {
        UUID id = UUID.randomUUID();
        Hackathon expected = new Hackathon();
        when(hackathonCrudService.getHackathonById(id)).thenReturn(expected);

        Hackathon actual = hackathonService.getHackathonById(id);

        assertSame(expected, actual);
        verify(hackathonCrudService).getHackathonById(id);
    }

    @Test
    void transitionHackathonStateDelegatesToLifecycleService() {
        UUID id = UUID.randomUUID();
        Hackathon expected = new Hackathon();
        when(hackathonLifecycleService.transitionHackathonState(id, HackathonState.ONGOING)).thenReturn(expected);

        Hackathon actual = hackathonService.transitionHackathonState(id, HackathonState.ONGOING);

        assertSame(expected, actual);
        verify(hackathonLifecycleService).transitionHackathonState(id, HackathonState.ONGOING);
    }

    @Test
    void assignWinnerDelegatesToLifecycleService() {
        UUID id = UUID.randomUUID();
        Team winner = new Team();
        Hackathon expected = new Hackathon();
        when(hackathonLifecycleService.assignWinner(id, winner)).thenReturn(expected);

        Hackathon actual = hackathonService.assignWinner(id, winner);

        assertSame(expected, actual);
        verify(hackathonLifecycleService).assignWinner(id, winner);
    }
}
