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

package org.da_scegliere.progetto_ids_hackathon.core.state.support;

import org.da_scegliere.progetto_ids_hackathon.core.enums.state.support.SupportRequestState;
import org.da_scegliere.progetto_ids_hackathon.core.state.common.StateRegistry;
import org.da_scegliere.progetto_ids_hackathon.core.state.support.state.SupportRequestLifecycleState;

import java.util.Objects;

/**
 * Default state-pattern implementation for support-request transitions.
 */
public final class DefaultSupportRequestLifecycleStateMachine implements SupportRequestLifecycleStateMachine {

    private final StateRegistry<SupportRequestState, SupportRequestLifecycleState> stateRegistry;

    public DefaultSupportRequestLifecycleStateMachine(
            StateRegistry<SupportRequestState, SupportRequestLifecycleState> stateRegistry
    ) {
        this.stateRegistry = Objects.requireNonNull(stateRegistry, "stateRegistry must not be null.");
    }

    @Override
    public SupportRequestState transition(SupportRequestState currentState, SupportRequestState targetState) {
        Objects.requireNonNull(targetState, "targetState must not be null.");
        if (currentState == null) {
            throw new IllegalStateException("Support request state is not initialized.");
        }
        if (currentState == targetState) {
            return currentState;
        }

        SupportRequestLifecycleState state = stateRegistry.get(currentState);
        return state.transitionTo(targetState);
    }
}
