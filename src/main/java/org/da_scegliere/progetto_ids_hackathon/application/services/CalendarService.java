/*
 * Authors:  Alejandro Innocenzi, Matteo Vittori, Vladislav Gaspari
 * Copyright (c) 2026 Alejandro Innocenzi, Matteo Vittori, Vladislav Gaspari. All rights reserved.
 *
 * This file is part of the DA_SCEGLIERE project. Unauthorized copying,
 * distribution, modification, or use of this file, via any medium,
 * is strictly prohibited unless in compliance with the license.
 *
 * Licensed under the MIT License:
 *     - Permission is hereby granted, free of charge, to any person obtaining
 *       a copy of this software and associated documentation files (the "Software"),
 *       to deal in the Software without restriction, including without limitation
 *       the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *       and/or sell copies of the Software, and to permit persons to whom the
 *       Software is furnished to do so, subject to the following conditions:
 *
 *     - The above copyright notice and this permission notice shall be included
 *       in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.da_scegliere.progetto_ids_hackathon.application.services;

import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.CalendarStrategy;
import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.exceptions.CalendarProviderConflictException;
import org.da_scegliere.progetto_ids_hackathon.application.ports.strategies.exceptions.CalendarProviderUnavailableException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.calendar.CalendarConflictException;
import org.da_scegliere.progetto_ids_hackathon.application.services.exceptions.calendar.CalendarUnavailableException;
import org.da_scegliere.progetto_ids_hackathon.core.support.SupportRequest;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Application service that orchestrates interactions with the external calendar provider.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Validate support-call requests before invoking external integrations.</li>
 *     <li>Expose UC-oriented operations for slot availability checks and call proposal.</li>
 *     <li>Translate provider-specific failures into application-level exceptions.</li>
 * </ul>
 * This service does not implement calendar conflict rules directly: conflict detection and
 * reservation semantics are delegated to the configured {@link CalendarStrategy}.
 */
@Service
public class CalendarService {

    private final CalendarStrategy calendarStrategy;

    /**
     * Creates a new service instance.
     *
     * @param calendarStrategy strategy adapter used to interact with the external calendar provider.
     * @throws NullPointerException when {@code calendarStrategy} is {@code null}.
     */
    public CalendarService(CalendarStrategy calendarStrategy) {
        this.calendarStrategy =
                Objects.requireNonNull(calendarStrategy, "calendarStrategy must not be null");
    }

    /**
     * Proposes a mentor call for the given support request (UC-M-02).
     * <p>
     * The operation performs input/date validation, checks slot availability and reserves the slot
     * through the external provider when available.
     *
     * @param request support request containing the desired call slot information.
     * @throws IllegalArgumentException when the request is invalid or the requested date is in the past.
     * @throws CalendarConflictException when the slot is already occupied.
     * @throws CalendarUnavailableException when the calendar provider is unavailable.
     */
    public void proposeCall(SupportRequest request) {
        SupportRequest safeRequest = requireValidRequest(request);

        try {
            boolean slotAvailable = calendarStrategy.isSlotAvailable(safeRequest);
            if (!slotAvailable) {
                throw new CalendarConflictException("Requested call slot is already occupied.");
            }
            calendarStrategy.reserveCallSlot(safeRequest);
        } catch (CalendarProviderConflictException ex) {
            throw new CalendarConflictException("Requested call slot is already occupied.", ex);
        } catch (CalendarProviderUnavailableException ex) {
            throw new CalendarUnavailableException("Calendar provider is unavailable.", ex);
        }
    }

    /**
     * Checks if the slot contained in the support request is currently available.
     *
     * @param request support request containing the desired call slot information.
     * @return {@code true} if the slot is available; {@code false} otherwise.
     * @throws IllegalArgumentException when the request is invalid or the requested date is in the past.
     * @throws CalendarConflictException when the provider reports a conflict for the requested slot.
     * @throws CalendarUnavailableException when the calendar provider is unavailable.
     */
    public boolean isSlotAvailable(SupportRequest request) {
        SupportRequest safeRequest = requireValidRequest(request);

        try {
            return calendarStrategy.isSlotAvailable(safeRequest);
        } catch (CalendarProviderConflictException ex) {
            throw new CalendarConflictException("Requested call slot is already occupied.", ex);
        } catch (CalendarProviderUnavailableException ex) {
            throw new CalendarUnavailableException("Calendar provider is unavailable.", ex);
        }
    }

    /**
     * Validates that a call date is not in the past.
     *
     * @param callDate date requested for the call slot.
     * @throws NullPointerException when {@code callDate} is {@code null}.
     * @throws IllegalArgumentException when {@code callDate} is before today.
     */
    public void validateCallDate(LocalDate callDate) {
        Objects.requireNonNull(callDate, "callDate must not be null.");
        if (callDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("callDate must be today or in the future.");
        }
    }

    private SupportRequest requireValidRequest(SupportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null.");
        }
        validateCallDate(request.getRequestedCallDate());
        return request;
    }
}
