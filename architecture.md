# Architecture

## Overview

StockPanel is a JavaFX 21 desktop application using a pure MVC pattern without Spring. All dependencies are manually wired through constructor injection and method-based dependency passing.

```
src/main/java/tech/zaky/stockpanel/
├── StockPanelApplication.java    # Entry point & lifecycle
├── Navigator.java                # Screen routing & dependency wiring
├── Screens.java                  # Screen enum (LOGIN, REGISTER, DASHBOARD, PORTFOLIO_DETAIL)
├── controllers/
│   ├── LoginController.java
│   ├── RegisterController.java
│   ├── DashboardController.java
│   └── PortfolioDetailController.java
├── models/
│   ├── User.java
│   ├── Deposit.java
│   ├── ReturnRecord.java
│   ├── Holding.java
│   └── enums/ReturnType.java
├── repositories/
│   ├── UserRepository.java
│   ├── DepositRepository.java
│   ├── ReturnRecordRepository.java
│   └── HoldingRepository.java
├── components/
│   └── FormattedDatePicker.java
└── utils/
    ├── UserSession.java
    ├── NumberFormatter.java
    └── CryptoMachine.java
```

---

## Application Lifecycle

### 1. Bootstrap (`StockPanelApplication.java`)

```
main() → launch() → start(Stage)
```

Inside `start()`:

1. **Hibernate Session initialization** — `setupSession()` builds a `SessionFactory` from a `StandardServiceRegistry` and registers all JPA entity classes manually:
   ```java
   session = new MetadataSources(registry)
       .addAnnotatedClass(Deposit.class)
       .addAnnotatedClass(Holding.class)
       .addAnnotatedClass(ReturnRecord.class)
       .addAnnotatedClass(User.class)
       .buildMetadata()
       .buildSessionFactory()
       .openSession();
   ```

2. **AtlantaFX theme** — `NordDark` is applied globally via `Application.setUserAgentStylesheet(...)`.

3. **Initial scene** — Loads `login.fxml`, creates the `Navigator`, and injects dependencies into `LoginController`.

4. **Shutdown** — `stop()` closes the Hibernate session.

### 2. Database Configuration (`hibernate.properties`)

- Embedded H2 with AES encryption stored at `~/AppData/Roaming/stockpanel/core.mv.db`
- Schema auto-managed via `hibernate.hbm2ddl.auto=update`
- No `persistence.xml` — configuration is fully properties-based

---

## Dependency Wiring

There is no DI container. The `Navigator` acts as the central dependency factory.

### Flow

```
StockPanelApplication
  └─ creates Navigator(scene, session)

Navigator.navigate(Screen)
  └─ loads FXML
  └─ gets controller from FXMLLoader
  └─ instantiates repositories with the shared Hibernate Session
  └─ calls controller.injectDependencies(...)
```

### Entity Registration

All JPA entities must be explicitly registered in `StockPanelApplication.setupSession()` via `addAnnotatedClass()`. Adding a new entity requires:

1. Create the `@Entity` class in `models/`
2. Add `.addAnnotatedClass(NewEntity.class)` in `setupSession()`

### Repository Instantiation

Repositories are plain classes that receive the Hibernate `Session` via constructor:

```java
new DepositRepository(session)
```

They are instantiated fresh inside each `Navigator.loadXxx()` method. The `Session` is a single long-lived instance shared across the app lifetime.

### Controller Dependency Injection

Each controller exposes an `injectDependencies(...)` method called by the Navigator after FXML loading:

```java
// Example: DashboardController
public void injectDependencies(
    DepositRepository depositRepository,
    ReturnRecordRepository returnRecordRepository,
    HoldingRepository holdingRepository,
    Navigator navigator) { ... }
```

---

## Screen Navigation

`Screens` is an enum. `Navigator.navigate(Screens)` switches the scene root:

| Screen             | FXML                   | Controller                  |
|--------------------|------------------------|-----------------------------|
| `LOGIN`            | `login.fxml`           | `LoginController`           |
| `REGISTER`         | `register.fxml`        | `RegisterController`        |
| `DASHBOARD`        | `dashboard.fxml`       | `DashboardController`       |
| `PORTFOLIO_DETAIL` | `portfolio_detail.fxml`| `PortfolioDetailController` |

Adding a new screen:
1. Add value to `Screens` enum
2. Create FXML + Controller
3. Add `loadXxx()` method in `Navigator`
4. Add case to `navigate()` switch

---

## Data Models (JPA Entities)

| Entity         | Table      | Relationship     | Key Fields                          |
|----------------|------------|------------------|-------------------------------------|
| `User`         | `users`    | —                | `username`, `password`              |
| `Deposit`      | `deposits` | `ManyToOne User` | `amount` (BigDecimal), `depositDate`, `notes` |
| `ReturnRecord` | `returns`  | `ManyToOne User` | `amount` (BigDecimal), `type` (SALE/DIVIDEND), `ticker`, `returnDate` |
| `Holding`      | `holdings` | `OneToOne User`  | `amount` (BigDecimal)               |

All financial fields use `BigDecimal` with `precision=19, scale=4` mapped to H2 `DECIMAL`.

---

## Observer Pattern (Dashboard)

The `DashboardController` uses a `ListChangeListener` on the `ObservableList<TransactionRow>` to reactively update metric cards without re-querying the database:

```
transactions (ObservableList)
  └─ ListChangeListener → updateCards()
       └─ streams transactions to compute totals
       └─ updates totalDepositsLabel, totalReturnsLabel, netStandingLabel
```

CRUD actions (add deposit, add return, delete) mutate `transactions` directly. The listener fires and recalculates card values automatically.

---

## User Session

`UserSession` is a static holder for the currently authenticated `User` object:

```java
UserSession.set(user);   // on login
UserSession.get();       // in controllers to scope queries
UserSession.clear();     // on logout
```

---

## Adding a New Feature Checklist

1. **New entity** → Create in `models/`, register in `setupSession()`
2. **New repository** → Create in `repositories/`, accepts `Session` in constructor
3. **New screen** → Add to `Screens` enum, create FXML + Controller, wire in `Navigator`
4. **New utility** → Place in `utils/` or `components/`
5. **Module system** → Export new packages in `module-info.java` if needed
