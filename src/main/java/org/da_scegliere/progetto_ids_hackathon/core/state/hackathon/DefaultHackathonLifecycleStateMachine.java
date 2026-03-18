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

package org.da_scegliere.progetto_ids_hackathon.core.state.hackathon;

import org.da_scegliere.progetto_ids_hackathon.core.enums.state.hackathon.HackathonState;
import org.da_scegliere.progetto_ids_hackathon.core.state.common.StateRegistry;
import org.da_scegliere.progetto_ids_hackathon.core.state.hackathon.state.HackathonLifecycleState;

import java.util.Objects;

/**
 * Default state-pattern implementation for hackathon lifecycle transitions.
 */
public final class DefaultHackathonLifecycleStateMachine implements HackathonLifecycleStateMachine {

    private final StateRegistry<HackathonState, HackathonLifecycleState> stateRegistry;

    public DefaultHackathonLifecycleStateMachine( StateRegistry<HackathonState, HackathonLifecycleState> stateRegistry) {
        this.stateRegistry = Objects.requireNonNull(stateRegistry, "stateRegistry must not be null.");
    }

    @Override
    public HackathonState transition(HackathonState currentState, HackathonState targetState) {
        Objects.requireNonNull(targetState, "targetState must not be null.");
        if (currentState == null) {
            if (targetState == HackathonState.REGISTRATION) {
                return HackathonState.REGISTRATION;
            }
            throw invalidTransition(null, targetState);
        }
        if (currentState == targetState) {
            return currentState;
        }

        HackathonLifecycleState state = stateRegistry.get(currentState);
        return state.transitionTo(targetState);
    }

    @Override
    public HackathonState next(HackathonState currentState) {
        if (currentState == null) {
            throw new IllegalStateException("Hackathon state is not initialized.");
        }
        HackathonLifecycleState state = stateRegistry.get(currentState);
        return state.next();
    }

    private static IllegalStateException invalidTransition(HackathonState from, HackathonState to) {
        return new IllegalStateException("Invalid state transition from " + from + " to " + to + ".");
    }
}
