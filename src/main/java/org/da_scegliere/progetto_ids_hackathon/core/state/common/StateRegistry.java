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

package org.da_scegliere.progetto_ids_hackathon.core.state.common;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Generic immutable registry that resolves handlers by state key.
 *
 * @param <S> state key type.
 * @param <H> handler type.
 */
public final class StateRegistry<S, H> {

    private final Map<S, H> handlersByState;
    private final Function<S, String> unsupportedStateMessageFactory;

    /**
     * Creates a registry from handlers and extraction strategy.
     *
     * @param handlers registered handlers.
     * @param stateExtractor function extracting state key from each handler.
     * @param unsupportedStateMessageFactory function producing unsupported-state message.
     */
    public StateRegistry(
            List<H> handlers,
            Function<H, S> stateExtractor,
            Function<S, String> unsupportedStateMessageFactory
    ) {
        Objects.requireNonNull(handlers, "handlers must not be null.");
        Objects.requireNonNull(stateExtractor, "stateExtractor must not be null.");
        this.unsupportedStateMessageFactory = Objects.requireNonNull(
                unsupportedStateMessageFactory,
                "unsupportedStateMessageFactory must not be null."
        );

        this.handlersByState = Map.copyOf(
                handlers.stream()
                        .collect(Collectors.toMap(
                                stateExtractor,
                                Function.identity(),
                                (first, second) -> {
                                    throw new IllegalStateException(
                                            "Duplicate state handler detected for state " + stateExtractor.apply(first) + "."
                                    );
                                }
                        ))
        );
    }

    /**
     * Resolves handler by state.
     *
     * @param state state key.
     * @return handler bound to the state.
     */
    public H get(S state) {
        H handler = handlersByState.get(state);
        if (handler == null) {
            throw new IllegalStateException(unsupportedStateMessageFactory.apply(state));
        }
        return handler;
    }
}
