package org.da_scegliere.progetto_ids_hackathon.application.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.da_scegliere.progetto_ids_hackathon.application.services.TeamInvitationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamInvitationScheduler {

    private final TeamInvitationService teamInvitationService;

    @Scheduled(cron = "${app.scheduler.team-invitation-expiration-cron:0 */10 * * * *}")
    public void processExpiredTeamCreationRequests() {
        int processed = teamInvitationService.expireOpenRequests();
        if (processed > 0) {
            log.info("Processed {} expired team creation requests.", processed);
        }
    }
}

