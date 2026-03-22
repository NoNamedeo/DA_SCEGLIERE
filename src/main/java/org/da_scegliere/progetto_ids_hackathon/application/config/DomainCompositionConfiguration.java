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

package org.da_scegliere.progetto_ids_hackathon.application.config;

import org.da_scegliere.progetto_ids_hackathon.application.config.properties.ClockProperties;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.support.SupportRequestState;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.user.AccountState;
import org.da_scegliere.progetto_ids_hackathon.core.policies.BusinessPolicy;
import org.da_scegliere.progetto_ids_hackathon.core.policies.hackathon.winner.WinnerAssignmentContext;
import org.da_scegliere.progetto_ids_hackathon.core.policies.hackathon.winner.WinnerAssignmentPolicy;
import org.da_scegliere.progetto_ids_hackathon.core.policies.support.SupportRequestMentorSelectionContext;
import org.da_scegliere.progetto_ids_hackathon.core.policies.support.SupportRequestMentorSelectionPolicy;
import org.da_scegliere.progetto_ids_hackathon.core.policies.team.LeaveTeamContext;
import org.da_scegliere.progetto_ids_hackathon.core.policies.team.LeaveTeamPolicy;
import org.da_scegliere.progetto_ids_hackathon.core.state.common.StateRegistry;
import org.da_scegliere.progetto_ids_hackathon.core.state.support.DefaultSupportRequestLifecycleStateMachine;
import org.da_scegliere.progetto_ids_hackathon.core.state.support.SupportRequestLifecycleStateMachine;
import org.da_scegliere.progetto_ids_hackathon.core.state.support.state.InProgressSupportRequestState;
import org.da_scegliere.progetto_ids_hackathon.core.state.support.state.OpenSupportRequestState;
import org.da_scegliere.progetto_ids_hackathon.core.state.support.state.RejectedSupportRequestState;
import org.da_scegliere.progetto_ids_hackathon.core.state.support.state.ResolvedSupportRequestState;
import org.da_scegliere.progetto_ids_hackathon.core.state.support.state.SupportRequestLifecycleState;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.AccountLifecycleStateMachine;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.DefaultAccountLifecycleStateMachine;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.state.AccountLifecycleState;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.state.ActiveAccountState;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.state.RevokedAccountState;
import org.da_scegliere.progetto_ids_hackathon.core.state.user.state.SuspendedAccountState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

/**
 * Composition root for domain state machines and policy implementations.
 */
@Configuration
@EnableConfigurationProperties(ClockProperties.class)
public class DomainCompositionConfiguration {

    @Bean
    public Clock clock(ClockProperties clockProperties) {
        ZoneId zone = clockProperties.getZone() == null
                ? ZoneId.systemDefault()
                : clockProperties.getZone();

        if (clockProperties.getMode() == ClockProperties.Mode.FIXED) {
            Instant fixedInstant = clockProperties.getFixedInstant();
            if (fixedInstant == null) {
                throw new IllegalStateException(
                        "app.clock.fixed-instant must be provided when app.clock.mode=FIXED."
                );
            }
            return Clock.fixed(fixedInstant, zone);
        }

        return Clock.system(zone);
    }

    @Bean
    public SupportRequestLifecycleStateMachine supportRequestLifecycleStateMachine() {
        StateRegistry<SupportRequestState, SupportRequestLifecycleState> stateRegistry = new StateRegistry<>(
                List.of(
                        new OpenSupportRequestState(),
                        new InProgressSupportRequestState(),
                        new ResolvedSupportRequestState(),
                        new RejectedSupportRequestState()
                ),
                SupportRequestLifecycleState::getState,
                state -> "Unsupported support request state: " + state + "."
        );
        return new DefaultSupportRequestLifecycleStateMachine(stateRegistry);
    }

    @Bean
    public AccountLifecycleStateMachine accountLifecycleStateMachine() {
        StateRegistry<AccountState, AccountLifecycleState> stateRegistry = new StateRegistry<>(
                List.of(
                        new ActiveAccountState(),
                        new SuspendedAccountState(),
                        new RevokedAccountState()
                ),
                AccountLifecycleState::getState,
                state -> "Unsupported account status: " + state + "."
        );
        return new DefaultAccountLifecycleStateMachine(stateRegistry);
    }

    @Bean
    public BusinessPolicy<WinnerAssignmentContext> winnerAssignmentPolicy() {
        return new WinnerAssignmentPolicy();
    }

    @Bean
    public BusinessPolicy<SupportRequestMentorSelectionContext> supportRequestMentorSelectionPolicy() {
        return new SupportRequestMentorSelectionPolicy();
    }

    @Bean
    public BusinessPolicy<LeaveTeamContext> leaveTeamPolicy() {
        return new LeaveTeamPolicy();
    }
}
