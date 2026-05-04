package org.da_scegliere.progetto_ids_hackathon.presentation.controller;

import lombok.RequiredArgsConstructor;
import org.da_scegliere.progetto_ids_hackathon.application.services.CalendarService;
import org.da_scegliere.progetto_ids_hackathon.application.services.views.calendar.CalendarView;
import org.da_scegliere.progetto_ids_hackathon.presentation.dto.response.calendar.CalendarResponse;
import org.da_scegliere.progetto_ids_hackathon.presentation.mapper.CalendarMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/calendars")
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/teams/{teamId}")
    public ResponseEntity<CalendarResponse> getTeamCalendar(@PathVariable UUID teamId) {
        CalendarView calendar = calendarService.getTeamCalendar(teamId);
        return ResponseEntity.ok(CalendarMapper.toResponse(calendar));
    }

    @GetMapping("/mentors/{mentorAssignmentId}")
    public ResponseEntity<CalendarResponse> getMentorCalendar(@PathVariable UUID mentorAssignmentId) {
        CalendarView calendar = calendarService.getMentorCalendar(mentorAssignmentId);
        return ResponseEntity.ok(CalendarMapper.toResponse(calendar));
    }
}
