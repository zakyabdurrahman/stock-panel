# System Specification: Personal Portfolio Capital Tracker (Native Desktop App)

## 1. Project Overview & "North Star"
* **The "Why":** A lightweight, ultra-secure native desktop application designed to track net capital allocations, returns (realized gains and dividends), and current asset holdings. By moving away from the web, the app requires zero network overhead, keeps financial data 100% local, and starts up instantly.
* **Target Audience:** Internal/Personal use with local multi-user profile switching.
* **Tech Stack:**
  - **UI Layer (View):** JavaFX 21+ (Styled via **AtlantaFX CSS themes**, layout structuring using FXML)
  - **Controller/Application Layer:** Standard Java 17+ core implementing a pure Model-View-Controller (MVC) pattern.
  - **Data Access Layer (Model):** Custom **Repository Pattern** implementations interacting directly with Hibernate ORM (Core/JPA) via standard JDBC and an embedded **H2 Database**.

---

## 2. Desktop Architecture & Data Isolation

By removing the Spring framework, the application relies on standard Java design patterns. JavaFX FXML controllers instantiate or look up domain-specific Repositories to mediate between the UI and the data mapping layer.

```
[ JavaFX View (FXML / AtlantaFX CSS) ] ---> [ JavaFX FXML Controller ] ---> [ Repository Layer (Custom Repos) ] ---> [ Hibernate / JDBC ] ---> [ H2 Embedded File (.mv.db) ]
```

### Application Lifecycle & Context Handshake
1. The standard Java `main` method launches the JavaFX `Application` class.
2. During the JavaFX `init()` phase, a custom database manager initializes the Hibernate `EntityManagerFactory`.
3. **AtlantaFX Theme Initialization:** Inside the JavaFX `start(Stage stage)` method, the AtlantaFX dark theme must be registered globally before loading layouts:
   ```java
   // Programmatically setting up AtlantaFX Primer Dark theme
   Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
   ```
4. The `FXMLLoader` loads views normally. On application shutdown (`stop()`), the Hibernate `EntityManagerFactory` is explicitly closed, and an explicit `SHUTDOWN` command is issued to H2.

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

### Phase 1: JavaFX App Bootstrapping & AtlantaFX Dependency Setup
* Configure a standard `persistence.xml` using the H2 JDBC driver.
* Include the `atlantafx-intentions` dependency in your build tool (`pom.xml` / `build.gradle`).
* Verify that launching an empty window with `PrimerDark` or `NordDark` stylesheet applied instantly defaults components to an edge-to-edge hardware-accelerated dark viewport interface.

### Phase 2: Local Login & Stage Navigation (MVC Setup)
* Build a clean JavaFX FXML login scene and bind it to a dedicated controller (`LoginController`).
* Implement a plain Java `UserRepository` class to handle user lookup and creation.

### Phase 3: Transaction Entry Controls (Forms & Modals)
* Build UI dialog forms or panels using JavaFX input fields for adding deposits and performance returns, wired to specific FXML actions.
* Leverage AtlantaFX input validation style pseudo-classes (e.g., adding classes like `.success` or `.danger` dynamically based on code feedback).

### Phase 4: Financial Terminal Grid Summary
* Implement collection and aggregation methods in `DepositRepository` and `ReturnRecordRepository` using JPQL queries.
* Add an optimized, striping-supported AtlantaFX style `TableView` beneath the metric blocks displaying transaction histories.

### Phase 5: Local Share Holdings Inventory & Historical Visualizer
* Implement a `HoldingRepository` handling upsert operations.
* Wire up the Clickable card component logic to swap screens seamlessly into the historical chart engine visualization panel.

---

## 5. UI/UX Page Specifications & AtlantaFX Class Mapping

Rather than manual custom colors, UI components are styled directly inside FXML using AtlantaFX's CSS Helper Utility Classes (`styleClass`).

### 5.1 Register Screen (`register.fxml`)
* **Purpose:** The initial entry point for a first-time user to set up credentials.
* **Layout Structure:** Centered `VBox` card containing a title banner, text input fields, and action buttons.
* **AtlantaFX Styling Polish:**
  - The wrapping card component uses `styleClass="card"`.
  - Title text uses `styleClass="title-2"`.
  - Input fields use `styleClass="rounded"` for modern smooth tracking profiles.
  - Form actions call `styleClass="button, success, large"`.
  - Error/validation labels use `styleClass="text-danger"`.

### 5.2 Login Screen (`login.fxml`)
* **Purpose:** Unlocks an existing local user profile.
* **Layout Structure:** Mirror card layout of the register screen to maintain visual consistency.
* **AtlantaFX Styling Polish:**
  - Login main action execution button utilizes `styleClass="button, accent, raised"`.
  - Hyperlinks map to standard `styleClass="link"`.

### 5.3 Dashboard View (`dashboard.fxml`)
* **Purpose:** The core terminal window displaying live metrics, transactional histories, and asset positions.
* **Layout Structure:** Root `BorderPane`. Top Menu Bar handles session controls. Left Sidebar holds asset listings. Center Grid tracks performance history.
* **UI Sub-Components with AtlantaFX Classes:**
  - **Top Menu Bar:** Implemented using a native `ToolBar`. Contains a right-aligned **"Logout"** component mapped to `styleClass="button, danger, flat, left-icon"`.
  - **Metric Summary Cards (Top Center):** A horizontal `HBox` tracking three individual data widgets wrapped in `.card` structures:
    1. *Total Capital Injected:* Styled with `styleClass="text-muted, title-4"` for headings. Contains a small `Button` with `styleClass="button, success, sm"` labeled **"+"** to trigger the Allocation Modal.
    2. *Total Capital Gains:* Tracks raw return metrics. Contains a small `Button` with `styleClass="button, accent, sm"` labeled **"+"** to trigger the Returns Modal.
    3. *Net Portfolio Standing / Gain Card:* Interactive clickable button element via `styleClass="card, button, interactive"`.
  - **Transaction Terminal Grid (Bottom Center):** A virtualized JavaFX `TableView` enforcing `styleClass="striped, bordered, compact"`.
  - **Holdings Sidebar Inventory (Left Panel):** A dedicated `VBox` wrapper. Uses an elegant `ListView` element set to `styleClass="dense"`.
* **Controller Logic (`DashboardController`):** Reads from `UserSession` to isolate queries. Binds transactional item collections to a dynamic JavaFX `ObservableList`. Intercepts the **"+"** button actions to programmatically launch the secondary modal input `Stage` layers using `Modality.APPLICATION_MODAL`.

### 5.4 Portfolio Detail View (`portfolio_detail.fxml`)
* **Purpose:** Deep-dive graphical analysis screen to visualize historical wealth tracking over time.
* **Layout Structure:** Root `BorderPane` layout housing a historical charting engine.
* **UI Sub-Components with AtlantaFX Classes:**
  - **Top Navigation Banner:** Features an explicit back navigation `Button` on the far left mapped to `styleClass="button, flat, body-strong"`.
  - **Main Viewport Panel:** A prominent JavaFX `LineChart<String, Number>` graph control. The chart coordinates naturally adapt to the active stylesheet framework theme colors, allowing trace series to stand out vibrantly against the dark base background.

### 5.5 Quick Entry Modals (`deposit_modal.fxml` & `return_modal.fxml`)
* **Purpose:** Focused, lightweight modal overlay windows that capture user transactional inputs securely and validate data locally before database writing.
* **Layout Structure:** An isolated `VBox` wrapper with structural padding (`padding="20"`), utilizing a clean form sheet design pattern.

#### A. Capital Allocation Modal (`deposit_modal.fxml`)
* **UI Components:**
  - `Label` header using `styleClass="title-3"` text reading *"Add Capital Allocation"*.
  - `TextField` for allocation amount (`promptText="0.00"`, `styleClass="rounded"`).
  - `DatePicker` for chronological date capture.
  - `TextArea` for descriptive notes (`promptText="Optional allocation notes..."`, `prefHeight="80"`).
  - An `HBox` alignment layout hosting two control buttons: **"Cancel"** (`styleClass="button, flat"`) and **"Save Allocation"** (`styleClass="button, success, raised"`).
* **Controller Logic (`DepositModalController`):**
  - Validates that the input amount is a positive numeric string using a regex or a `try-catch` block wrapping `new BigDecimal(text)`. If string parsing fails, it adds the `.danger` pseudo-class to the input box instantly.
  - On submission success, maps input states directly into a new `Deposit` JPA model instance, executes `depositRepository.persist(deposit)`, appends the fresh record entity straight back into the main Dashboard's active `ObservableList`, and triggers `stage.close()`.

#### B. Capital Return Modal (`return_modal.fxml`)
* **UI Components:**
  - `Label` header using `styleClass="title-3"` text reading *"Add Performance Return"*.
  - `TextField` for asset identifier tracking (`promptText="e.g. AAPL"`, `styleClass="rounded"`).
  - `ComboBox<ReturnType>` dropdown selector or an AtlantaFX alternative `ToggleGroup` switch determining asset activity framework type (`SALE` vs `DIVIDEND`).
  - `TextField` for raw transactional currency yield (`promptText="0.00"`).
  - `DatePicker` for execution timeline logging.
  - Control button pairing layout containing **"Cancel"** and **"Save Return"** (`styleClass="button, accent, raised"`).
* **Controller Logic (`ReturnModalController`):**
  - Forces ticker string normalization into strict uppercase characters using `.toUpperCase().trim()`.
  - Interacts directly with database layers through transactional pipelines inside `ReturnRecordRepository` and updates user index logs dynamically across local data layers without requiring manual terminal restarts.

---

## 6. UI/UX Expectations & Desktop Styling
* **Theme Preference Selection:** Configured using standard **AtlantaFX Primer Dark** or **Nord Dark** out-of-the-box asset stylesheets. 
* **Vibe:** Minimalist, highly modern engineering interface—resembles look-and-feel traits found in desktop tools like GitHub Desktop or modern IDEs. Flat design aesthetics, dark gray cards (`#1c2128` scale variations), vibrant accent highlights, zero execution lag.

---

## 7. Constraints & "Do Not Do" List
* **DO NOT** use Spring Boot, Spring Data, or Spring Dependency Injection.
* **DO NOT** write long, fragile custom CSS stylesheets to build layout backgrounds. Rely on standard AtlantaFX framework rules and pre-built global element utility style tags.
* **DO NOT** use raw floating-point types (`double`, `float`) for H2 financial interactions. Ensure Hibernate maps `BigDecimal` cleanly to H2's high-precision `DECIMAL` types.
* **DO NOT** use heavy external cloud tooling. Everything must stay fully contained within the executable Java runtime package and local H2 storage file.
