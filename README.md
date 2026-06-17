# StockPanel 

A lightweight, ultra-secure native desktop application designed to track net capital allocations, realized returns (gains from selling stock), and dividends.

![Alt](https://raw.githubusercontent.com/zakyabdurrahman/stock-panel/refs/heads/main/screenshots/s1.png)

Most mainstream retail stock trading apps (like Ajaib) only display the fluctuating value of your *current active holdings*. They fail to cleanly aggregate and show your total lifetime performance—leaving out **realized gains from stocks you've already sold** and **passive cash flow generated from dividends**.

**StockPanel** fixes this. It acts as a dedicated personal capital terminal, allowing you to track your true financial footprint without relying on cluttered spreadsheets or cloud-connected tools.

### Key Design Pillars

* **100% Local Privacy:** No cloud accounts, no external syncs, and zero network overhead. Your financial history stays entirely inside an encrypted, embedded local database.
* **Instant Boot Performance:** Built natively using JavaFX—starting up instantly with a minimalist, IDE-like engineering interface.
* **Financial Precision:** Strict use of high-precision calculations to eliminate rounding errors common in standard trading app UI summaries.

---

## 🛠️ Tech Stack & Architecture

StockPanel is deliberately decoupled from heavy frameworks like Spring to ensure a zero-lag desktop footprint. It utilizes a pure **Model-View-Controller (MVC)** pattern backed by strong standard enterprise Java layers:

* **UI Layer (View):** JavaFX 21+ managed via FXML layouts.
* **UI Styling:** [AtlantaFX](https://github.com/mkpaz/atlantafx) CSS Themes (**Primer Dark** / **Nord Dark** ecosystem) for modern, native-feeling utility styling.
* **Controller/Application Layer:** Pure Java 17+ core with custom **Repository Pattern** implementations.
* **Data Access Layer (Model):** Hibernate ORM (Core/JPA) via native JDBC.
* **Database:** Embedded **H2 Database Engine** saving locally to an isolated `.mv.db` file.

```
[ JavaFX View (FXML / AtlantaFX) ] 
               │
               ▼
   [ JavaFX FXML Controller ] 
               │
               ▼
  [ Repository Layer (Custom) ] 
               │
               ▼
     [ Hibernate / JDBC ] 
               │
               ▼
   [ H2 Embedded File (.mv.db) ]

```

---

##  Features & Interface Specifications

###  1. Local Profile Management

* Multi-user profile switching via a localized login & registration screen.
* Passwords are securely managed locally for profile locking.

###  2. Financial Terminal Dashboard

* **Metric Summary Cards:**
* *Total Capital Injected:* Tracks all cash allocations put into the market.
* *Total Capital Gains:* Combines cash flowing back from stock sales and dividend payments.
* *Net Portfolio Standing:* Calculates your true bottom-line position via an interactive, clickable card element.


* **Transaction Terminal Grid:** A virtualized, striped historical log capturing and displaying every past transaction instantly.

###  3. Quick Entry Modals

* **Capital Allocation Form:** Easily log deposits with custom historical tracking dates and contextual notes.
* **Performance Return Form:** Log stock sales (`SALE`) or cash dividends (`DIVIDEND`). 

###  4. Portfolio Detail Visualizer

* Seamlessly transition from the interactive dashboard standing card into a dedicated historical analysis view.
* Features a hardware-accelerated JavaFX charting engine mapped beautifully to the active dark mode theme palette to visualize wealth tracking over time.

---



## 🚀 Getting Started

### Prerequisites

* **Java JDK 17 or 21+ with JavaFX Bundled** *(Recommended: **Azul Zulu JDK FX** or **BellSoft Liberica JDK Full**. A standard JDK will throw runtime errors unless the JavaFX modules are explicitly added to the path).*
* **Maven** or **Gradle**



### Running the Application

1. Clone the repository.
2. Build the project executable bundle using your build automation tool.
3. Launch the application main entry point:

```bash
mvn clean javafx:run

```

*Upon launch, the application will automatically initialize the local `persistence.xml` configuration, configure the H2 storage files in your local directory, and load the dark viewport layout wrapper.*

---

## 📄 License

This project is configured for internal and personal portfolio tracking use.