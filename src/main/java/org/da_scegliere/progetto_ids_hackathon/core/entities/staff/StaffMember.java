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

package org.da_scegliere.progetto_ids_hackathon.core.entities.staff;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import org.da_scegliere.progetto_ids_hackathon.core.entities.user.AbstractUser;
import org.da_scegliere.progetto_ids_hackathon.core.events.staff.StaffMemberCreatedEvent;
import org.da_scegliere.progetto_ids_hackathon.core.events.staff.StaffMemberNameChangedEvent;

import java.util.List;
import java.util.Objects;

@Getter
@Entity
public class StaffMember extends AbstractUser{

    @OneToMany(mappedBy = "staffMember", cascade = CascadeType.ALL)
    private List<StaffAssignment> staffAssignmentList;

    public StaffMember(String name, int age, String email, List<StaffAssignment> staffAssignmentList) {
        super(name, age, email);
        this.staffAssignmentList = staffAssignmentList;
    }

    public StaffMemberCreatedEvent toCreatedEvent() {
        return new StaffMemberCreatedEvent(this);
    }

    public StaffMemberNameChangedEvent renameAndCreateNameChangedEvent(String newName) {
        Objects.requireNonNull(newName, "newName must not be null.");
        if (newName.isBlank()) {
            throw new IllegalArgumentException("newName must not be blank.");
        }
        setName(newName);
        return new StaffMemberNameChangedEvent(this, newName);
    }

    public StaffMember() {}
}
