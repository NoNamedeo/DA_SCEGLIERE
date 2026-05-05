# Hackathon Management System

Backend Spring Boot per la gestione completa del ciclo di vita di un hackathon, con supporto a:
- utenti, team, staff, manager
- partecipazioni e submission
- valutazione e assegnazione vincitore
- support request e moderation report
- notifiche e scheduler automatici

## Web Interface
Frontend ufficiale (React + Tailwind):  
[https://marvelous-cajeta-1a68ff.netlify.app/](https://marvelous-cajeta-1a68ff.netlify.app/)

## Avvio Frontend + Backend
Per usare il frontend con il backend locale:

1. Avvia il backend Spring Boot sulla porta `8080`
```bash
./mvnw spring-boot:run
```

2. Apri il frontend dal browser:
[https://marvelous-cajeta-1a68ff.netlify.app/](https://marvelous-cajeta-1a68ff.netlify.app/)

3. Quando il browser mostra il popup `Access other apps and services on this device`, accettalo.

4. Il frontend parlera' con il backend locale su `http://localhost:8080`.

5. Se il backend non e' ancora attivo o la porta `8080` e' occupata, il frontend non potra' completare le chiamate API.

### Verifica rapida
- Backend: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- H2 console: `http://localhost:8080/h2-console`

## Stack Tecnologico
- Java 21
- Spring Boot 4.0.3
- Spring Web, Spring Data JPA, Validation, Actuator
- H2 Database (file mode)
- Springdoc OpenAPI / Swagger UI
- Lombok

## Architettura
Il progetto segue Clean Architecture con dipendenze verso l’interno:

```text
core            -> dominio (entita, value object, policy, state machine)
application     -> use case orchestration (services, ports, scheduler, config)
infrastructure  -> adapter tecnici (JPA repository, strategy esterne, event publisher)
presentation    -> API REST (controller, DTO, mapper, error handling)
```

## Struttura Progetto
```text
src/main/java/org/da_scegliere/progetto_ids_hackathon
├── core
│   ├── entities
│   ├── enums
│   ├── events
│   ├── policies
│   └── state
├── application
│   ├── config
│   ├── factory
│   ├── listeners
│   ├── ports
│   ├── scheduler
│   └── services
├── infrastructure
│   ├── events
│   ├── jpa/repositories
│   └── strategies
└── presentation
    ├── controller
    ├── dto
    ├── error
    └── mapper
```

## Modello di Dominio
Entità principali:
- `Hackathon`
- `User`
- `Manager`
- `StaffMember`
- `StaffAssignment`
- `Team`
- `TeamParticipation`
- `Submission`
- `SupportRequest`
- `ModerationReport` (`UserReport`, `StaffReport`)
- `Notification` (`BaseNotification`, `TeamInviteNotification`)

## Pattern Utilizzati
- Strategy Pattern
  - `CalendarStrategy`
  - `PaymentStrategy`
- Builder Pattern
  - `HackathonBuilder`
  - `HackathonBuilderDirector`
- Domain Events + Listener
  - Eventi dominio pubblicati dai service/domain
  - Listener transazionali per side-effect (es. notifiche)

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
- API base: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- H2 Console: `http://localhost:8080/h2-console`

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
app.scheduler.hackathon-lifecycle-cron=*/30 * * * * *
app.scheduler.team-invitation-expiration-cron=0 */10 * * * *
```

## Qualità API
- Error handling centralizzato via `GlobalExceptionHandler`
- Payload error uniforme (`ApiErrorResponse`) con `errorId`, `code`, `violations`
- Validazione input via Bean Validation + regole dominio

## Convenzioni REST
- base path versionato: `/api/v1`
- risorse al plurale (`/users`, `/teams`, `/hackathons`, ...)
- query params per filtri
- status code coerenti (`201` con `Location`, `204` su delete/no body, ecc.)

## Troubleshooting Rapido
- `The given id must not be null` su create hackathon:
  - verificare payload `creatorId`
  - assicurarsi che il creator esista e sia coerente col flusso staff assignment
- Swagger 500 su `/v3/api-docs`:
  - controllare stacktrace startup e mapping duplicati controller/DTO
- mismatch schema H2:
  - fermare app
  - eliminare file DB locale (`~/hackathon-db.mv.db`)
  - riavviare per rigenerare schema e seed

## Autori
- Alejandro Innocenzi
- Matteo Vittori
- Vladislav Gaspari

## Licenza
MIT License
