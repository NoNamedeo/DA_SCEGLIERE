-- ============================================================
-- DEV REFRESH SEED (H2)
-- Idempotent seed for structured manual API testing.
-- Runs safely multiple times: it deletes only known fixed IDs,
-- then re-inserts a stable baseline dataset.
-- ============================================================

-- ------------------------------------------------------------
-- Fixed IDs map
-- ------------------------------------------------------------
-- USERS
--   USER_ANNA_TEAM_ALPHA     = 10000000-0000-0000-0000-000000000001
--   USER_LUCA_TEAM_BETA      = 10000000-0000-0000-0000-000000000002
--   USER_MIA_NO_TEAM         = 10000000-0000-0000-0000-000000000003
--
-- STAFF
--   STAFF_SARA               = 20000000-0000-0000-0000-000000000001
--   STAFF_MATTEO             = 20000000-0000-0000-0000-000000000002
--
-- MANAGER
--   MANAGER_MARIO            = 30000000-0000-0000-0000-000000000001
--
-- TEAMS
--   TEAM_ALPHA               = 40000000-0000-0000-0000-000000000001
--   TEAM_BETA                = 40000000-0000-0000-0000-000000000002
--
-- HACKATHONS
--   HACKATHON_REGISTRATION   = 50000000-0000-0000-0000-000000000001
--   HACKATHON_ONGOING        = 50000000-0000-0000-0000-000000000002
--   HACKATHON_EVALUATION     = 50000000-0000-0000-0000-000000000003
--
-- PARTICIPATIONS
--   PART_ONGOING_ALPHA       = 60000000-0000-0000-0000-000000000001
--   PART_EVAL_ALPHA          = 60000000-0000-0000-0000-000000000002
--   PART_EVAL_BETA           = 60000000-0000-0000-0000-000000000003
--
-- SUBMISSIONS
--   SUB_ONGOING_ALPHA        = 70000000-0000-0000-0000-000000000001
--   SUB_EVAL_ALPHA_EVALUATED = 70000000-0000-0000-0000-000000000002
--   SUB_EVAL_BETA_PENDING    = 70000000-0000-0000-0000-000000000003
--
-- STAFF ASSIGNMENTS
--   SA_REG_ORGANIZER         = 80000000-0000-0000-0000-000000000001
--   SA_ONGOING_MENTOR        = 80000000-0000-0000-0000-000000000002
--   SA_EVAL_JUDGE            = 80000000-0000-0000-0000-000000000003
--
-- REPORTS (base + subclass share same ID)
--   USER_REPORT_OPEN         = 90000000-0000-0000-0000-000000000001
--   STAFF_REPORT_OPEN        = 90000000-0000-0000-0000-000000000002
--
-- SUPPORT REQUEST
--   SUPPORT_REQ_OPEN         = a0000000-0000-0000-0000-000000000001

-- ------------------------------------------------------------
-- Compatibility cleanup for old local schema leftovers
-- ------------------------------------------------------------
ALTER TABLE PUBLIC.STAFF_MEMBER DROP COLUMN IF EXISTS SUSPENDED;
ALTER TABLE PUBLIC.USERS DROP COLUMN IF EXISTS SUSPENDED;
ALTER TABLE PUBLIC.MANAGER DROP COLUMN IF EXISTS SUSPENDED;
ALTER TABLE PUBLIC.HACKATHON DROP COLUMN IF EXISTS HACKATHON_STATE;

-- ------------------------------------------------------------
-- DELETE phase (only fixed IDs, deterministic refresh)
-- ------------------------------------------------------------
DELETE FROM PUBLIC.SUPPORT_REQUEST_MENTORS
WHERE support_request_id = UUID 'a0000000-0000-0000-0000-000000000001'
   OR staff_assignment_id IN (
    UUID '80000000-0000-0000-0000-000000000001',
    UUID '80000000-0000-0000-0000-000000000002',
    UUID '80000000-0000-0000-0000-000000000003'
    );

DELETE FROM PUBLIC.SUPPORT_REQUEST
WHERE id = UUID 'a0000000-0000-0000-0000-000000000001';

DELETE FROM PUBLIC.SUBMISSION
WHERE id IN (
    UUID '70000000-0000-0000-0000-000000000001',
    UUID '70000000-0000-0000-0000-000000000002',
    UUID '70000000-0000-0000-0000-000000000003'
    );

DELETE FROM PUBLIC.PARTICIPATION
WHERE id IN (
    UUID '60000000-0000-0000-0000-000000000001',
    UUID '60000000-0000-0000-0000-000000000002',
    UUID '60000000-0000-0000-0000-000000000003'
    );

DELETE FROM PUBLIC.STAFF_ASSIGNMENT
WHERE id IN (
    UUID '80000000-0000-0000-0000-000000000001',
    UUID '80000000-0000-0000-0000-000000000002',
    UUID '80000000-0000-0000-0000-000000000003'
    );

DELETE FROM PUBLIC.USER_REPORT
WHERE id = UUID '90000000-0000-0000-0000-000000000001';

DELETE FROM PUBLIC.STAFF_REPORT
WHERE id = UUID '90000000-0000-0000-0000-000000000002';

DELETE FROM PUBLIC.MODERATION_REPORT
WHERE id IN (
    UUID '90000000-0000-0000-0000-000000000001',
    UUID '90000000-0000-0000-0000-000000000002'
    );

DELETE FROM PUBLIC.BASE_NOTIFICATION
WHERE target_id IN (
    UUID '10000000-0000-0000-0000-000000000001',
    UUID '10000000-0000-0000-0000-000000000002',
    UUID '10000000-0000-0000-0000-000000000003',
    UUID '20000000-0000-0000-0000-000000000001',
    UUID '20000000-0000-0000-0000-000000000002',
    UUID '30000000-0000-0000-0000-000000000001'
    );

UPDATE PUBLIC.HACKATHON
SET winner_team_id = NULL
WHERE id IN (
    UUID '50000000-0000-0000-0000-000000000001',
    UUID '50000000-0000-0000-0000-000000000002',
    UUID '50000000-0000-0000-0000-000000000003'
    );

DELETE FROM PUBLIC.HACKATHON
WHERE id IN (
    UUID '50000000-0000-0000-0000-000000000001',
    UUID '50000000-0000-0000-0000-000000000002',
    UUID '50000000-0000-0000-0000-000000000003'
    );

DELETE FROM PUBLIC.USERS
WHERE id IN (
    UUID '10000000-0000-0000-0000-000000000001',
    UUID '10000000-0000-0000-0000-000000000002',
    UUID '10000000-0000-0000-0000-000000000003'
    );

DELETE FROM PUBLIC.STAFF_MEMBER
WHERE id IN (
    UUID '20000000-0000-0000-0000-000000000001',
    UUID '20000000-0000-0000-0000-000000000002'
    );

DELETE FROM PUBLIC.MANAGER
WHERE id = UUID '30000000-0000-0000-0000-000000000001';

DELETE FROM PUBLIC.TEAM
WHERE id IN (
    UUID '40000000-0000-0000-0000-000000000001',
    UUID '40000000-0000-0000-0000-000000000002'
    );

-- ------------------------------------------------------------
-- INSERT phase
-- ------------------------------------------------------------
INSERT INTO PUBLIC.TEAM (id, name)
VALUES
    (UUID '40000000-0000-0000-0000-000000000001', 'Team Alpha'),
    (UUID '40000000-0000-0000-0000-000000000002', 'Team Beta');

INSERT INTO PUBLIC.USERS (
    id, name, age, email, account_status, moderation_note, account_status_updated_at, team_id
)
VALUES
    (
        UUID '10000000-0000-0000-0000-000000000001',
        'Anna Rossi',
        24,
        'anna.rossi@example.com',
        'ACTIVE',
        NULL,
        TIMESTAMP '2026-03-20 10:00:00',
        UUID '40000000-0000-0000-0000-000000000001'
    ),
    (
        UUID '10000000-0000-0000-0000-000000000002',
        'Luca Bianchi',
        26,
        'luca.bianchi@example.com',
        'ACTIVE',
        NULL,
        TIMESTAMP '2026-03-20 10:05:00',
        UUID '40000000-0000-0000-0000-000000000002'
    ),
    (
        UUID '10000000-0000-0000-0000-000000000003',
        'Mia Verdi',
        23,
        'mia.verdi@example.com',
        'ACTIVE',
        NULL,
        TIMESTAMP '2026-03-20 10:10:00',
        NULL
    );

INSERT INTO PUBLIC.STAFF_MEMBER (
    id, name, age, email, account_status, moderation_note, account_status_updated_at
)
VALUES
    (
        UUID '20000000-0000-0000-0000-000000000001',
        'Sara Neri',
        34,
        'sara.neri@example.com',
        'ACTIVE',
        NULL,
        TIMESTAMP '2026-03-20 10:15:00'
    ),
    (
        UUID '20000000-0000-0000-0000-000000000002',
        'Matteo Vittori',
        21,
        'matteo.vittori@example.com',
        'ACTIVE',
        NULL,
        TIMESTAMP '2026-03-20 10:16:00'
    );

INSERT INTO PUBLIC.MANAGER (
    id, name, age, email, account_status, moderation_note, account_status_updated_at
)
VALUES
    (
        UUID '30000000-0000-0000-0000-000000000001',
        'Mario Conti',
        45,
        'mario.conti@example.com',
        'ACTIVE',
        NULL,
        TIMESTAMP '2026-03-20 10:20:00'
    );

INSERT INTO PUBLIC.HACKATHON (
    id,
    name,
    description,
    winner_team_id,
    award_prize,
    prize_paid_at,
    registration_deadline,
    submission_deadline,
    evaluation_deadline
)
VALUES
    (
        UUID '50000000-0000-0000-0000-000000000001',
        'Hackathon Registration',
        'Hackathon in REGISTRATION phase for testing enrolment and staff assignment.',
        NULL,
        1500.00,
        NULL,
        DATE '2026-03-30',
        DATE '2026-04-05',
        DATE '2026-04-10'
    ),
    (
        UUID '50000000-0000-0000-0000-000000000002',
        'Hackathon Ongoing',
        'Hackathon in ONGOING phase for testing submission create/update flows.',
        NULL,
        2500.00,
        NULL,
        DATE '2026-03-20',
        DATE '2026-03-29',
        DATE '2026-04-03'
    ),
    (
        UUID '50000000-0000-0000-0000-000000000003',
        'Hackathon Evaluation',
        'Hackathon in EVALUATION phase for testing submission evaluation and winner flows.',
        NULL,
        3000.00,
        NULL,
        DATE '2026-03-10',
        DATE '2026-03-22',
        DATE '2026-03-29'
    );

INSERT INTO PUBLIC.PARTICIPATION (
    dtype,
    id,
    entry_date,
    nickname,
    hackathon_id,
    team_id
)
VALUES
    (
        'TeamParticipation',
        UUID '60000000-0000-0000-0000-000000000001',
        DATE '2026-03-21',
        'alpha-ongoing',
        UUID '50000000-0000-0000-0000-000000000002',
        UUID '40000000-0000-0000-0000-000000000001'
    ),
    (
        'TeamParticipation',
        UUID '60000000-0000-0000-0000-000000000002',
        DATE '2026-03-12',
        'alpha-eval',
        UUID '50000000-0000-0000-0000-000000000003',
        UUID '40000000-0000-0000-0000-000000000001'
    ),
    (
        'TeamParticipation',
        UUID '60000000-0000-0000-0000-000000000003',
        DATE '2026-03-12',
        'beta-eval',
        UUID '50000000-0000-0000-0000-000000000003',
        UUID '40000000-0000-0000-0000-000000000002'
    );

INSERT INTO PUBLIC.SUBMISSION (
    id,
    submitted_at,
    description,
    title,
    judge_score,
    judge_judgement,
    evaluated_at,
    team_participation_id
)
VALUES
    (
        UUID '70000000-0000-0000-0000-000000000001',
        DATE '2026-03-24',
        'Ongoing submission draft for Team Alpha.',
        'Alpha Ongoing Project',
        NULL,
        NULL,
        NULL,
        UUID '60000000-0000-0000-0000-000000000001'
    ),
    (
        UUID '70000000-0000-0000-0000-000000000002',
        DATE '2026-03-18',
        'Team Alpha evaluation submission.',
        'Alpha Evaluation Project',
        8,
        'Good architecture and completeness.',
        DATE '2026-03-24',
        UUID '60000000-0000-0000-0000-000000000002'
    ),
    (
        UUID '70000000-0000-0000-0000-000000000003',
        DATE '2026-03-19',
        'Team Beta evaluation submission pending judge score.',
        'Beta Evaluation Project',
        NULL,
        NULL,
        NULL,
        UUID '60000000-0000-0000-0000-000000000003'
    );

INSERT INTO PUBLIC.STAFF_ASSIGNMENT (
    id,
    assignment_date,
    staff_role,
    staff_member_id,
    hackathon_id
)
VALUES
    (
        UUID '80000000-0000-0000-0000-000000000001',
        DATE '2026-03-20',
        'ORGANIZER',
        UUID '20000000-0000-0000-0000-000000000001',
        UUID '50000000-0000-0000-0000-000000000001'
    ),
    (
        UUID '80000000-0000-0000-0000-000000000002',
        DATE '2026-03-22',
        'MENTOR',
        UUID '20000000-0000-0000-0000-000000000001',
        UUID '50000000-0000-0000-0000-000000000002'
    ),
    (
        UUID '80000000-0000-0000-0000-000000000003',
        DATE '2026-03-23',
        'JUDGE',
        UUID '20000000-0000-0000-0000-000000000001',
        UUID '50000000-0000-0000-0000-000000000003'
    );

INSERT INTO PUBLIC.SUPPORT_REQUEST (
    id,
    date_slot,
    state,
    accepting_mentor_id,
    sending_team_id
)
VALUES
    (
        UUID 'a0000000-0000-0000-0000-000000000001',
        DATE '2026-03-27',
        'OPEN',
        NULL,
        UUID '40000000-0000-0000-0000-000000000001'
    );

INSERT INTO PUBLIC.SUPPORT_REQUEST_MENTORS (
    support_request_id,
    staff_assignment_id
)
VALUES
    (
        UUID 'a0000000-0000-0000-0000-000000000001',
        UUID '80000000-0000-0000-0000-000000000002'
    );

INSERT INTO PUBLIC.MODERATION_REPORT (
    id,
    title,
    description,
    reporter_id,
    reporter_type,
    state,
    processed_by_manager_id,
    created_at,
    processed_at,
    processing_notes
)
VALUES
    (
        UUID '90000000-0000-0000-0000-000000000001',
        'Spam in chat',
        'User repeatedly sends spam links in team channel.',
        UUID '10000000-0000-0000-0000-000000000001',
        'USER',
        'OPEN',
        NULL,
        TIMESTAMP '2026-03-25 09:00:00',
        NULL,
        NULL
    ),
    (
        UUID '90000000-0000-0000-0000-000000000002',
        'Staff unavailable',
        'Assigned mentor missed scheduled support calls.',
        UUID '10000000-0000-0000-0000-000000000001',
        'USER',
        'OPEN',
        NULL,
        TIMESTAMP '2026-03-25 09:05:00',
        NULL,
        NULL
    );

INSERT INTO PUBLIC.USER_REPORT (
    id,
    reported_user_id
)
VALUES
    (
        UUID '90000000-0000-0000-0000-000000000001',
        UUID '10000000-0000-0000-0000-000000000002'
    );

INSERT INTO PUBLIC.STAFF_REPORT (
    id,
    reported_staff_member_id
)
VALUES
    (
        UUID '90000000-0000-0000-0000-000000000002',
        UUID '20000000-0000-0000-0000-000000000001'
    );

-- ------------------------------------------------------------
-- Quick checks (optional, safe to keep)
-- ------------------------------------------------------------
-- SELECT id, name, email, team_id FROM PUBLIC.USERS ORDER BY id;
-- SELECT id, name FROM PUBLIC.TEAM ORDER BY id;
-- SELECT id, name, registration_deadline, submission_deadline, evaluation_deadline FROM PUBLIC.HACKATHON ORDER BY id;
-- SELECT id, dtype, hackathon_id, team_id FROM PUBLIC.PARTICIPATION ORDER BY id;
-- SELECT id, team_participation_id, judge_score FROM PUBLIC.SUBMISSION ORDER BY id;
-- SELECT id, staff_role, hackathon_id FROM PUBLIC.STAFF_ASSIGNMENT ORDER BY id;
-- SELECT id, state, sending_team_id FROM PUBLIC.SUPPORT_REQUEST ORDER BY id;
-- SELECT id, reporter_type, state FROM PUBLIC.MODERATION_REPORT ORDER BY id;
