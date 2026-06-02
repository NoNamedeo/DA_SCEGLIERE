/*
 * Authors:  Alejandro Innocenzi, Matteo Vittori
 * Copyright (c) 2026 Alejandro Innocenzi, Matteo Vittori. All rights reserved.
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

package org.da_scegliere.progetto_ids_hackathon.core.entities.moderation;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.da_scegliere.progetto_ids_hackathon.core.entities.staff.StaffMember;
import org.da_scegliere.progetto_ids_hackathon.core.enums.report.ReporterType;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.Manager;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.report.UserReportState;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base for moderation reports.
 * <p>
 * The base captures lifecycle and audit data shared by all report types
 * (user reports, content reports, event reports, and similar future extensions).
 */
@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ModerationReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private UUID reporterId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ReporterType reporterType;

    @NotNull
    @Enumerated(EnumType.STRING)
    private UserReportState state;

    @ManyToOne
    @JoinColumn(name = "processed_by_manager_id")
    private Manager processedBy;

    @ManyToOne
    @JoinColumn(name = "processed_by_staff_member_id")
    private StaffMember processedByStaffMember;

    @NotNull
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private String processingNotes;

    protected ModerationReport(UUID reporterId, ReporterType reporterType, String title, String description) {
        this.reporterId = Objects.requireNonNull(reporterId, "reporterId must not be null.");
        this.reporterType = Objects.requireNonNull(reporterType, "reporterType must not be null.");
        this.title = requireNonBlank(title, "title");
        this.description = requireNonBlank(description, "description");
        this.state = UserReportState.OPEN;
        this.createdAt = LocalDateTime.now();
        this.processedAt = null;
        this.processingNotes = null;
        this.processedBy = null;
        this.processedByStaffMember = null;
    }

    protected ModerationReport() {
    }

    public void accept(Manager manager, String notes) {
        ensureOpen();
        this.processedBy = Objects.requireNonNull(manager, "manager must not be null.");
        this.processedByStaffMember = null;
        this.processingNotes = requireNonBlank(notes, "notes");
        this.state = UserReportState.ACCEPTED;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(Manager manager, String notes) {
        ensureOpen();
        this.processedBy = Objects.requireNonNull(manager, "manager must not be null.");
        this.processedByStaffMember = null;
        this.processingNotes = requireNonBlank(notes, "notes");
        this.state = UserReportState.REJECTED;
        this.processedAt = LocalDateTime.now();
    }

    public void acceptByStaffMember(StaffMember staffMember, String notes) {
        ensureOpen();
        this.processedByStaffMember = Objects.requireNonNull(staffMember, "staffMember must not be null.");
        this.processedBy = null;
        this.processingNotes = requireNonBlank(notes, "notes");
        this.state = UserReportState.ACCEPTED;
        this.processedAt = LocalDateTime.now();
    }

    public void rejectByStaffMember(StaffMember staffMember, String notes) {
        ensureOpen();
        this.processedByStaffMember = Objects.requireNonNull(staffMember, "staffMember must not be null.");
        this.processedBy = null;
        this.processingNotes = requireNonBlank(notes, "notes");
        this.state = UserReportState.REJECTED;
        this.processedAt = LocalDateTime.now();
    }

    public boolean isOpen() {
        return this.state == UserReportState.OPEN;
    }

    private void ensureOpen() {
        if (!isOpen()) {
            throw new IllegalStateException("Moderation report has already been processed.");
        }
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank.");
        }
        return value.trim();
    }
}
