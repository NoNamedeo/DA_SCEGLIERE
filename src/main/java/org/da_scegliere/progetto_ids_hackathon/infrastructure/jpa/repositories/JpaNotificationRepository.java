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

package org.da_scegliere.progetto_ids_hackathon.infrastructure.jpa.repositories;

import org.da_scegliere.progetto_ids_hackathon.application.ports.repositories.INotificationRepository;
import org.da_scegliere.progetto_ids_hackathon.core.entities.notification.AbstractNotification;
import org.da_scegliere.progetto_ids_hackathon.core.entities.notification.TeamInviteNotification;
import org.da_scegliere.progetto_ids_hackathon.core.enums.state.team.TeamInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaNotificationRepository extends JpaRepository<AbstractNotification, UUID>, INotificationRepository {

    @Override
    @Query("select n from TeamInviteNotification n where n.id = :invitationId and n.target.id = :inviteeId")
    Optional<TeamInviteNotification> findTeamInvitationByIdAndTarget_Id(
            @Param("invitationId") UUID invitationId,
            @Param("inviteeId") UUID inviteeId
    );

    @Override
    @Query("select n from TeamInviteNotification n where n.requestId = :requestId")
    List<TeamInviteNotification> findTeamInvitationsByRequestId(@Param("requestId") UUID requestId);

    @Override
    @Query("select n from TeamInviteNotification n where n.creatorId = :creatorId")
    List<TeamInviteNotification> findTeamInvitationsByCreatorId(@Param("creatorId") UUID creatorId);

    @Override
    @Query("select n from TeamInviteNotification n where n.target.id = :inviteeId")
    List<TeamInviteNotification> findTeamInvitationsByTargetId(@Param("inviteeId") UUID inviteeId);

    @Override
    @Query("select n from TeamInviteNotification n where n.target.id = :inviteeId and n.invitationStatus = :status")
    List<TeamInviteNotification> findTeamInvitationsByTargetIdAndStatus(
            @Param("inviteeId") UUID inviteeId,
            @Param("status") TeamInvitationStatus status
    );

    @Override
    @Query("select n from TeamInviteNotification n where n.invitationStatus = :status")
    List<TeamInviteNotification> findTeamInvitationsByStatus(@Param("status") TeamInvitationStatus status);
}
