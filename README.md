# Hackathon Management System

## Overview

Questo progetto implementa un sistema backend per la gestione completa del ciclo di vita di un hackathon, progettato per supportare scenari realistici e complessi che coinvolgono diversi attori e flussi operativi.

La piattaforma consente la creazione e amministrazione di hackathon. Attorno all’hackathon ruotano diverse entità di dominio, tra cui utenti, manager e membri dello staff, ciascuno con responsabilità e comportamenti distinti.

Gli utenti possono organizzarsi in team, partecipare agli hackathon e sottomettere i propri progetti. Il sistema gestisce l’intero processo di partecipazione, incluse le submission, la loro valutazione e l’eventuale assegnazione dei vincitori, regolata da policy esplicite di business. Un aspetto rilevante del sistema è la gestione delle support request e moderation report, che permettono ai team di richiedere assistenza durante l’hackathon o di segnalare altri utenti.

Sono presenti anche funzionalità trasversali come:

un sistema di notifiche per comunicazioni tra piattaforma e utenti
integrazione con servizi esterni (ad esempio calendari e pagamenti) tramite pattern Strategy
gestione centralizzata degli errori e delle eccezioni

Dal punto di vista architetturale, il progetto adotta la Clean Architecture per isolare il dominio dalle dipendenze esterne.

Questa organizzazione consente di ottenere un sistema altamente modulare, testabile ed estendibile, in cui le regole di business sono esplicite e facilmente evolvibili senza impattare le componenti tecniche.---

---

## Architecture

Il progetto segue la Clean Architecture con separazione in layer:

```text
core            → dominio (entità, stati, business policy)
application     → use cases + orchestrazione
infrastructure  → dettagli tecnici (JPA, provider esterni)
presentation    → REST API (controller, DTO, mapper)
```

## Project Structure

```text
application/
├── ports/
│   ├── repositories/     → interfacce repository
│   └── strategies/       → interfacce per servizi esterni
├── services/             → use cases applicativi
├── scheduler/            → job automatici
└── config/               → configurazione (clock, DI)

core/
├── entities/             → modelli di dominio
├── enums/                → stati e tipi
├── state/                → state machines
└── policies/             → business rules (Specification/Policy)

infrastructure/
├── jpa/repositories/     → implementazioni repository
└── strategies/           → integrazioni esterne

presentation/
├── controller/           → REST API
├── dto/                  → request/response
├── mapper/               → mapping domain ↔ DTO
└── error/                → gestione errori globali
```

---

## Domain Model

Principali entità:

* **Hackathon**
* **User**
* **Manager**
* **StaffMember**
* **Team**
* **TeamParticipation**
* **Submission**
* **Notification**
* **SupportRequest**
* **StaffAssignment**
* **ModerationReport (User/Staff)**

---

## Design Patterns utilizzati

---

### 1. Strategy Pattern

Per integrazione con servizi esterni:

* `CalendarStrategy`
* `PaymentStrategy`

Implementazioni:

* Google Calendar
* PayPal

---

### 2. Builder Pattern

Per la creazione di hackathon:

* `HackathonBuilder`
* `HackathonBuilderDirector`

---

## Authors

* Alejandro Innocenzi
* Matteo Vittori
* Vladislav Gaspari

---

## License

MIT License
