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

package org.da_scegliere.progetto_ids_hackathon.core.entities.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.user.AccountState;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.AccountLifecycleStateMachine;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class AbstractUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(min = 2, max = 50)
    @Setter
    private String name;

    @Min(18)
    @Max(120)
    private int age;

    @NotBlank
    @Email
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AccountState accountStatus;

    @Setter
    @Size(max = 500)
    private String moderationNote;

    private LocalDateTime accountStatusUpdatedAt;

    protected AbstractUser(String name, int age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.accountStatus = AccountState.ACTIVE;
        this.moderationNote = null;
        this.accountStatusUpdatedAt = LocalDateTime.now();
    }

    protected AbstractUser() {}

    @PrePersist
    protected void initializeAccountStatusDefaults() {
        if (this.accountStatus == null) {
            this.accountStatus = AccountState.ACTIVE;
        }
        if (this.accountStatusUpdatedAt == null) {
            this.accountStatusUpdatedAt = LocalDateTime.now();
        }
    }

    public boolean isSuspended() {
        return accountStatus == AccountState.SUSPENDED;
    }

    public boolean isRevoked() {
        return accountStatus == AccountState.REVOKED;
    }

    public void suspend(String note, AccountLifecycleStateMachine stateMachine) {
        validateModerationNote(note);
        this.accountStatus = Objects.requireNonNull(stateMachine, "stateMachine must not be null.")
                .suspend(this.accountStatus);
        this.moderationNote = note.trim();
        this.accountStatusUpdatedAt = LocalDateTime.now();
    }

    public void reinstate(String note, AccountLifecycleStateMachine stateMachine) {
        validateModerationNote(note);
        this.accountStatus = Objects.requireNonNull(stateMachine, "stateMachine must not be null.")
                .reinstate(this.accountStatus);
        this.moderationNote = note.trim();
        this.accountStatusUpdatedAt = LocalDateTime.now();
    }

    public void revoke(String note, AccountLifecycleStateMachine stateMachine) {
        validateModerationNote(note);
        this.accountStatus = Objects.requireNonNull(stateMachine, "stateMachine must not be null.")
                .revoke(this.accountStatus);
        this.moderationNote = note.trim();
        this.accountStatusUpdatedAt = LocalDateTime.now();
    }

    private static void validateModerationNote(String note) {
        Objects.requireNonNull(note, "moderation note must not be null.");
        if (note.isBlank()) {
            throw new IllegalArgumentException("moderation note must not be blank.");
        }
    }
}
