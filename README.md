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
├── factory/              → factory (notification)
├── listeners/            → listeners (eventi di dominio)
└── config/               → configurazione (clock, DI)

core/
├── entities/             → modelli di dominio
├── enums/                → stati e tipi
├── events/               → eventi
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

## Prerequisiti
- JDK 21
- Maven (oppure wrapper `./mvnw`)

Verifica rapida:
```bash
java -version
./mvnw -v
```

## Installazione
```bash
git clone <repo-url>
cd DA_SCEGLIERE
./mvnw clean compile
```

## Esecuzione
Avvio applicazione:
```bash
./mvnw spring-boot:run
```

Build jar:
```bash
./mvnw clean package
java -jar target/Progetto_IdS_Hackathon-0.0.1-SNAPSHOT.jar
```

## Endpoint Utili in Locale
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

## Database e Seed
Configurazione default:
- URL: `jdbc:h2:file:~/hackathon-db;AUTO_SERVER=TRUE`
- user: `sa`
- password: vuota
- `spring.jpa.hibernate.ddl-auto=create-drop`
- seed automatico da `classpath:sql/dev-refresh-seed.sql`

Note:
- ad ogni avvio viene ricreato schema + seed (in dev)
- DB file in home: `~/hackathon-db.mv.db`

## Clock Testabile (fondamentale per lifecycle hackathon)
Il progetto usa `Clock` centralizzato (`DomainCompositionConfiguration`) e supporta due modalità:
- `SYSTEM`: tempo reale macchina
- `FIXED`: istante fisso deterministico (ottimo per test manuali e demo scheduler)

Proprietà (`application.properties`):
```properties
app.clock.mode=FIXED
app.clock.zone=Europe/Rome
app.clock.fixed-instant=2026-03-29T12:00:00Z
```

### Esempi pratici
Tempo reale:
```properties
app.clock.mode=SYSTEM
app.clock.zone=Europe/Rome
```

Tempo fisso per forzare stati hackathon:
```properties
app.clock.mode=FIXED
app.clock.zone=Europe/Rome
app.clock.fixed-instant=2026-04-10T10:00:00Z
```

Importante:
- se `app.clock.mode=FIXED`, `app.clock.fixed-instant` deve essere valorizzato
- il validator Bean Validation usa lo stesso `Clock`, quindi anche controlli `@PastOrPresent`, `@FutureOrPresent` restano coerenti

## Scheduler
Configurazioni disponibili:
```properties
app.scheduler.hackathon-lifecycle-cron=0 */5 * * * *  (ogni 5 minuti)
app.scheduler.team-invitation-expiration-cron=0 */10 * * * * (ogni 10 minuti)
```

---

## Authors

* Alejandro Innocenzi
* Matteo Vittori
* Vladislav Gaspari

---

## License

MIT License
