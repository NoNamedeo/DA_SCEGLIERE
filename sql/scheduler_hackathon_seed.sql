-- Seed minimo per verificare lo scheduler dell'hackathon.
-- Pensato per H2 con naming Spring/Hibernate di default.
--
-- Cosa crea:
-- 1. un hackathon gia' terminato temporalmente
-- 2. due team partecipanti
-- 3. una submission valutata per ciascun team
--
-- Risultato atteso con il codice attuale:
-- - lo scheduler trova l'hackathon in ENDED
-- - prova a concluderlo e poi ad assegnare il vincitore
-- - l'assegnazione del vincitore molto probabilmente fallisce
--   perche' il dominio consente assignWinner solo in EVALUATION
--
-- IDs fissi per test ripetibili
-- hackathon:    11111111-1111-1111-1111-111111111111
-- team alpha:   22222222-2222-2222-2222-222222222222
-- team beta:    33333333-3333-3333-3333-333333333333
-- part alpha:   44444444-4444-4444-4444-444444444444
-- part beta:    55555555-5555-5555-5555-555555555555
-- sub alpha:    66666666-6666-6666-6666-666666666666
-- sub beta:     77777777-7777-7777-7777-777777777777

DELETE FROM submission
WHERE id IN (
    UUID '66666666-6666-6666-6666-666666666666',
    UUID '77777777-7777-7777-7777-777777777777'
);

DELETE FROM participation
WHERE id IN (
    UUID '44444444-4444-4444-4444-444444444444',
    UUID '55555555-5555-5555-5555-555555555555'
);

UPDATE hackathon
SET winner_team_id = NULL
WHERE id = UUID '11111111-1111-1111-1111-111111111111';

DELETE FROM hackathon
WHERE id = UUID '11111111-1111-1111-1111-111111111111';

DELETE FROM team
WHERE id IN (
    UUID '22222222-2222-2222-2222-222222222222',
    UUID '33333333-3333-3333-3333-333333333333'
);

INSERT INTO team (id, name)
VALUES
    (UUID '22222222-2222-2222-2222-222222222222', 'Alpha Team'),
    (UUID '33333333-3333-3333-3333-333333333333', 'Beta Team');

INSERT INTO hackathon (
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
VALUES (
    UUID '11111111-1111-1111-1111-111111111111',
    'Scheduler Test Hackathon',
    'Hackathon creato manualmente per testare la chiusura automatica.',
    NULL,
    1000.00,
    NULL,
    DATE '2025-01-10',
    DATE '2025-01-20',
    DATE '2025-01-25'
);

-- Participation usa inheritance SINGLE_TABLE di default.
-- Il discriminatore DTYPE deve identificare il sottotipo TeamParticipation.
INSERT INTO participation (
    dtype,
    id,
    entry_date,
    nickname,
    hackathon_id,
    team_id,
    disqualified,
    disqualified_at,
    disqualification_reason
)
VALUES
    (
        'TeamParticipation',
        UUID '44444444-4444-4444-4444-444444444444',
        DATE '2025-01-12',
        'alpha-entry',
        UUID '11111111-1111-1111-1111-111111111111',
        UUID '22222222-2222-2222-2222-222222222222',
        FALSE,
        NULL,
        NULL
    ),
    (
        'TeamParticipation',
        UUID '55555555-5555-5555-5555-555555555555',
        DATE '2025-01-12',
        'beta-entry',
        UUID '11111111-1111-1111-1111-111111111111',
        UUID '33333333-3333-3333-3333-333333333333',
        FALSE,
        NULL,
        NULL
    );

INSERT INTO submission (
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
        UUID '66666666-6666-6666-6666-666666666666',
        DATE '2025-01-18',
        'Soluzione Alpha',
        'Alpha Project',
        8,
        'Buona soluzione',
        DATE '2025-01-24',
        UUID '44444444-4444-4444-4444-444444444444'
    ),
    (
        UUID '77777777-7777-7777-7777-777777777777',
        DATE '2025-01-18',
        'Soluzione Beta',
        'Beta Project',
        10,
        'Migliore submission del test',
        DATE '2025-01-24',
        UUID '55555555-5555-5555-5555-555555555555'
    );

-- Verifica immediata prima dello scheduler
SELECT
    h.id,
    h.name,
    h.registration_deadline,
    h.submission_deadline,
    h.evaluation_deadline,
    h.winner_team_id,
    h.prize_paid_at
FROM hackathon h
WHERE h.id = UUID '11111111-1111-1111-1111-111111111111';

SELECT
    p.id AS participation_id,
    p.nickname,
    p.team_id,
    s.id AS submission_id,
    s.judge_score,
    s.evaluated_at
FROM participation p
LEFT JOIN submission s
    ON s.team_participation_id = p.id
WHERE p.hackathon_id = UUID '11111111-1111-1111-1111-111111111111'
ORDER BY s.judge_score DESC;

-- Query da rieseguire dopo che lo scheduler e' partito
SELECT
    h.id,
    h.name,
    h.winner_team_id,
    h.prize_paid_at
FROM hackathon h
WHERE h.id = UUID '11111111-1111-1111-1111-111111111111';
