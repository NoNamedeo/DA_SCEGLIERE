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

### 3. State Pattern

Per modellare e validare le transizioni di stato del dominio senza concentrare tutta la logica in servizi monolitici o in blocchi condizionali ripetuti.

Il pattern è utilizzato principalmente per:

* ciclo di vita delle support request
* ciclo di vita degli account utente

Classi principali:

* `SupportRequestLifecycleStateMachine`
* `DefaultSupportRequestLifecycleStateMachine`
* `SupportRequestLifecycleState`
* `OpenSupportRequestState`
* `InProgressSupportRequestState`
* `ResolvedSupportRequestState`
* `RejectedSupportRequestState`
* `AccountLifecycleStateMachine`
* `DefaultAccountLifecycleStateMachine`
* `AccountLifecycleState`
* `ActiveAccountState`
* `SuspendedAccountState`
* `RevokedAccountState`
* `StateRegistry`

Ogni stato concreto incapsula le transizioni consentite a partire da quello stato. Le state machine risolvono lo stato corrente tramite `StateRegistry` e delegano allo stato concreto la decisione sulla transizione richiesta.

---

### 4. Observer Pattern tramite Spring Events

Per disaccoppiare le operazioni principali dalle reazioni secondarie, ad esempio la generazione di notifiche dopo eventi di dominio.

In fase di analisi il pattern è stato rappresentato in stile GoF, con publisher e observer espliciti. Nell'implementazione concreta, la registrazione degli observer è demandata al container Spring:

* `DomainEventPublisher` definisce la porta applicativa per pubblicare eventi
* `SpringDomainEventPublisher` adatta la porta a `ApplicationEventPublisher`
* `NotificationDomainEventListener` contiene i metodi listener
* `@TransactionalEventListener` registra in modo dichiarativo i metodi che osservano specifici eventi

Eventi osservati:

* `TeamCreatedEvent`
* `TeamDeletedEvent`
* `TeamMemberAddedEvent`
* `UserSuspendedEvent`
* `WinnerPrizePaidEvent`
* `HackathonConcludedEvent`
* `SupportRequestCreatedEvent`
* `SupportRequestAcceptedEvent`
* `SupportRequestRejectedEvent`

Questo approccio applica la logica dell'Observer Pattern, ma senza metodi manuali come `subscribe`, `unsubscribe` o `notifyObservers`, perché la sottoscrizione e il dispatch sono gestiti da Spring.

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

## Web Interface
Frontend ufficiale (React + Tailwind):  
[https://marvelous-cajeta-1a68ff.netlify.app/](https://marvelous-cajeta-1a68ff.netlify.app/)

## Avvio Frontend + Backend
Per usare il frontend con il backend locale:

1. Avvia il backend Spring Boot sulla porta `8080` (Default)
```bash
./mvnw spring-boot:run
```

2. Apri il frontend dal browser:
[https://marvelous-cajeta-1a68ff.netlify.app/](https://marvelous-cajeta-1a68ff.netlify.app/)

3. Quando il browser mostra il popup `Access other apps and services on this device`, accettalo.

4. Il frontend comunicherà con il backend locale su `http://localhost:8080`.

5. Se il backend non è ancora attivo o la porta `8080` è occupata, il frontend non potra' completare le chiamate API.

---

## Authors

* Alejandro Innocenzi
* Matteo Vittori
* Vladislav Gaspari

---

## License

MIT License
