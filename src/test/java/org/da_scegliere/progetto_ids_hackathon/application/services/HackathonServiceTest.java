package org.da_scegliere.progetto_ids_hackathon.application.services;

import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonCrudService;
import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonLifecycleService;
import org.da_scegliere.progetto_ids_hackathon.application.services.hackathon.HackathonStaffService;
import org.da_scegliere.progetto_ids_hackathon.core.entities.hackathon.Hackathon;
import org.da_scegliere.progetto_ids_hackathon.core.entities.team.Team;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;
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


    @Test
    void getHackathonByIdDelegatesToCrudService() {
        UUID id = UUID.randomUUID();
        Hackathon expected = new Hackathon();
        when(hackathonCrudService.getHackathonById(id)).thenReturn(expected);

        Hackathon actual = hackathonCrudService.getHackathonById(id);

        assertSame(expected, actual);
        verify(hackathonCrudService).getHackathonById(id);
    }

    @Test
    void determineCurrentStateDelegatesToLifecycleService() {
        UUID id = UUID.randomUUID();
        HackathonState expected = HackathonState.ONGOING;
        when(hackathonLifecycleService.determineCurrentState(id)).thenReturn(expected);

        HackathonState actual = hackathonLifecycleService.determineCurrentState(id);

        assertSame(expected, actual);
        verify(hackathonLifecycleService).determineCurrentState(id);
    }

    @Test
    void assignWinnerDelegatesToLifecycleService() {
        UUID id = UUID.randomUUID();
        Team winner = new Team();
        Hackathon expected = new Hackathon();
        when(hackathonLifecycleService.assignWinner(id, winner)).thenReturn(expected);

        Hackathon actual = hackathonLifecycleService.assignWinner(id, winner);

        assertSame(expected, actual);
        verify(hackathonLifecycleService).assignWinner(id, winner);
    }
}
