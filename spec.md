# System Specification: Personal Portfolio Capital Tracker (Native Desktop App)

## 1. Project Overview & "North Star"
* **The "Why":** A lightweight, ultra-secure native desktop application designed to track net capital allocations, returns (realized gains and dividends), and current asset holdings. By moving away from the web, the app requires zero network overhead, keeps financial data 100% local, and starts up instantly.
* **Target Audience:** Internal/Personal use with local multi-user profile switching.
* **Tech Stack:**
  - **UI Layer (View):** JavaFX 21+ (Styled via JavaFX CSS, layout structuring using FXML)
  - **Controller/Application Layer:** Standard Java 17+ core implementing a pure Model-View-Controller (MVC) pattern.
  - **Data Access Layer (Model):** Custom **Repository Pattern** implementations interacting directly with Hibernate ORM (Core/JPA) via standard JDBC and an embedded **H2 Database**.

---

## 2. Desktop Architecture & Data Isolation

By removing the Spring framework, the application relies on standard Java design patterns. JavaFX FXML controllers instantiate or look up domain-specific Repositories to mediate between the UI and the data mapping layer.

```
[ JavaFX View (FXML / CSS) ] ---> [ JavaFX FXML Controller ] ---> [ Repository Layer (Custom Repos) ] ---> [ Hibernate / JDBC ] ---> [ H2 Embedded File (.mv.db) ]
```

### Application Lifecycle & Context Handshake
1. The standard Java `main` method launches the JavaFX `Application` class.
2. During the JavaFX `init()` phase, a custom database manager initializes the Hibernate `EntityManagerFactory` (bootstrapping the persistence unit for H2).
3. The `FXMLLoader` loads views normally. Controllers handle user interactions and communicate with custom Java Repositories to query and save domain aggregates.
4. On application shutdown (`stop()`), the Hibernate `EntityManagerFactory` is explicitly closed, and an explicit `SHUTDOWN` command is issued to the H2 engine to safely flush, defragment, and lock the local storage files.

---

## 3. Database Schema (JPA Entities)

Financial data precision is strictly enforced in the app layer using Java's `BigDecimal`, which Hibernate maps seamlessly to H2's native, high-precision `DECIMAL` SQL data type.

### User Entity
```java
@Entity
@Table(name = "users")
public class User {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password; // Kept for local profile locking/hashing
}
```

### Deposit Entity (Money In)
```java
@Entity
@Table(name = "deposits")
public class Deposit {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne 
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDate depositDate;
    
    private String notes;
}
```

### Return Entity (Money Out)
```java
@Entity
@Table(name = "returns")
public class ReturnRecord {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne 
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    private ReturnType type; // SALE, DIVIDEND
    
    private String ticker; // Forced Uppercase
    
    @Column(nullable = false)
    private LocalDate returnDate;
}
```

### Holding Entity (Active Asset Inventory)
```java
@Entity
@Table(name = "holdings", uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "ticker"})})
public class Holding {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne 
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String ticker;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal sharesCount;
}
```

---

## 4. Feature Breakdown & Iterative Milestones

### Phase 1: JavaFX App Bootstrapping & Hibernate/H2 Connectivity
* Configure a standard `persistence.xml` using the H2 JDBC driver (`org.h2.Driver`), the H2 Hibernate dialect, and an embedded file URL connection string (e.g., `jdbc:h2:~/portfolio_tracker;DB_CLOSE_DELAY=-1`).
* Create a custom `HibernateUtil` singleton class to manage the setup and teardown of the `EntityManagerFactory`.

### Phase 2: Local Login & Stage Navigation (MVC Setup)
* Build a clean JavaFX FXML login scene and bind it to a dedicated controller (`LoginController`).
* Implement a plain Java `UserRepository` class to handle user lookup and creation directly via a manually managed Hibernate `EntityManager`.

### Phase 3: Transaction Entry Controls (Forms & Modals)
* Build UI dialog forms or panels using JavaFX input fields for adding deposits and performance returns, wired to specific FXML actions.
* Use custom `DepositRepository` and `ReturnRecordRepository` components to open an `EntityManager` transaction and persist entry models to H2 using standard JPA methods (`em.persist()`).

### Phase 4: Financial Terminal Grid Summary
* Implement collection and aggregation methods in `DepositRepository` and `ReturnRecordRepository` using JPQL queries (e.g., `SELECT SUM(d.amount) FROM Deposit d WHERE d.user = :user`) to fetch total metrics.
* Add a JavaFX `TableView` beneath the metric blocks displaying a unified list of chronological transaction entries bound to an `ObservableList`.

### Phase 5: Local Share Holdings Inventory & Historical Visualizer
* Implement a `HoldingRepository` handling upsert operations (checking if a ticker exists for the user domain, then creating or merging the record using Hibernate).
* Wire up the **Net Gain Card** selection event to transition into an interactive charting workspace mapping cumulative asset history.

---

## 5. UI/UX Page Specifications & View-Controller Mapping

Every screen is decoupled into an FXML file (View), a native JavaFX CSS stylesheet (Theme), and a concrete Java Controller class. 

### 5.1 Register Screen (`register.fxml`)
* **Purpose:** The initial entry point for a first-time user to set up their local offline profile credentials.
* **Layout Structure:** Centered `VBox` card containing a title banner, text input fields, and action buttons.
* **UI Components:**
  - `TextField` for `Username`.
  - `PasswordField` for `Password`.
  - `PasswordField` for `Confirm Password`.
  - `Label` for real-time validation feedback (e.g., "Passwords do not match").
  - `Button` for `Create Profile`.
  - `Hyperlink` to switch back to the Login screen.
* **Controller Logic (`RegisterController`):** Passes details to the `UserRepository` to check for uniqueness and persist the new `User` record with a hashed password, swapping the scene to the Login screen upon completion.

### 5.2 Login Screen (`login.fxml`)
* **Purpose:** Unlocks an existing local user profile and establishes the active domain context.
* **Layout Structure:** Mirror layout of the register screen to maintain visual consistency.
* **UI Components:**
  - `TextField` for `Username`.
  - `PasswordField` for `Password`.
  - `Label` for error alerts.
  - `Button` for `Unlock Workspace`.
  - `Hyperlink` labeled `Create New Profile`.
* **Controller Logic (`LoginController`):** Verifies the credentials against `UserRepository`. On success, instantiates the `UserSession` singleton with the logged-in `User` entity and changes the primary stage scene to `dashboard.fxml`.

### 5.3 Dashboard View (`dashboard.fxml`)
* **Purpose:** The core financial terminal window displaying live metrics, transactional histories, and asset positions.
* **Layout Structure:** Main Container is a root `BorderPane`. Top Menu Bar handles session controls. Left Sidebar holds asset listings. Center Grid tracks performance history.
* **UI Sub-Components:**
  - **Top Menu Bar (Right-Aligned Column):** Contains a stylized `Button` labeled **"Logout"** which clears the `UserSession` context and replaces the primary workspace stage back to `login.fxml`.
  - **Metric Summary Cards (Top Center):** A horizontal `HBox` tracking three individual data widgets:
    1. *Total Capital Injected Card:* Displays the raw aggregate sum of all deposits.
    2. *Total Capital Gains Card:* Displays the aggregate sum of all performance returns.
    3. *Net Portfolio Standing / Gain Card:* Interactive, clickable panel styled with CSS hover triggers (`-fx-cursor: hand`) linking directly to the analytics chart.
  - **Transaction Terminal Grid (Bottom Center):** A vertical-scrolling virtualized JavaFX `TableView` mapping columns directly to transaction entity records.
  - **Holdings Sidebar Inventory (Left Panel):** A dedicated `VBox` wrapping a structured `ListView` containing active asset tickers.
* **Controller Logic (`DashboardController`):** Reads from `UserSession` to isolate queries to the current user. Binds transactional item collections to a dynamic JavaFX `ObservableList`. Clicking the interactive Net Portfolio Standing card invokes layout swapping to the Portfolio Detail scene.

### 5.4 Portfolio Detail View (`portfolio_detail.fxml`)
* **Purpose:** Deep-dive graphical analysis screen to visualize historical wealth tracking over time.
* **Layout Structure:** Root `BorderPane` layout housing a historical charting engine.
* **UI Sub-Components:**
  - **Top Navigation Banner:** Features an explicit **"← Back to Dashboard"** navigation `Button` on the far left to return instantly to the primary desk interface.
  - **Main Viewport Panel:** A prominent JavaFX **`LineChart<String, Number>`** graph control. Includes a horizontal `CategoryAxis` mapping dates or chronological milestones and a vertical `NumberAxis` computing precision monetary value.
* **Controller Logic (`PortfolioDetailController`):** - On initialization, gathers chronological lists of user actions from `DepositRepository` and `ReturnRecordRepository`.
  - Runs a calculation pipeline creating a cumulative running total: Value at Time T = Sum of Deposits(<=T) + Sum of Gains(<=T).
  - Populates this chronological trajectory directly into an `XYChart.Series<String, Number>` data container bound straight to the `LineChart` frame for native, zero-lag desktop visualization.

---

## 6. UI/UX Expectations & Desktop Styling
* **Theme & Styling:** Done using native JavaFX stylesheets (`.css`). Dark Mode: Background `#0B0F19` (Deep Navy Midnight), Layout Panels: `#111827`.
* **Typography:** Clean modern system font sizing (Inter style if available).
* **Vibe:** Native financial tracker terminal—fast window resizing, zero rendering lag, instantaneous button actions.

---

## 7. Constraints & "Do Not Do" List
* **DO NOT** use Spring Boot, Spring Data, or Spring Dependency Injection. Use pure Java construction, factories, or manual singletons for dependency management.
* **DO NOT** use framework-generated repositories or standard DAO pattern naming conventions. Data operations must be encapsulated inside custom, concrete domain Repository classes written from scratch.
* **DO NOT** use network-facing REST architecture or spin up the H2 web console in production mode. All data handling stays within local memory method pipelines.
* **DO NOT** use raw floating-point types (`double`, `float`) for H2 financial interactions. Ensure Hibernate maps `BigDecimal` cleanly to H2's high-precision `DECIMAL` types.
* **DO NOT** use heavy external cloud tooling. Everything must stay fully contained within the executable Java runtime package and local H2 storage file.
