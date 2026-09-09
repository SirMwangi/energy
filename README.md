# Energy Android App

Energy is a comprehensive, modern Android application built with **Kotlin** and **Jetpack Compose**. It is designed to provide a robust and scalable platform for managing energy accounts and transactions, following industry-standard architectural patterns.

## Project Vision

The goal of the Energy app is to offer a seamless, high-performance user experience for tracking energy usage, balances, and financial transactions. It leverages a multi-layered architecture to ensure code maintainability, testability, and clear separation of concerns.

---

## 🏗 Architecture Overview

The project follows a **Layered Architecture** with the **Repository Pattern**, facilitating a clean separation between the UI and the data sources.

### 1. Presentation Layer (UI)
- **Framework**: Jetpack Compose (Declarative UI)
- **Design System**: Material Design 3 (M3)
- **Features**: Single Activity architecture, Edge-to-edge support, and custom theming.

### 2. Domain/Data Layer
- **Models**: Plain Old Kotlin Objects (POJOs) representing the core business entities.
- **Repositories**: Abstract the data sources (Local vs. Remote) and provide a clean API to the UI layer.
- **Local Data (Room)**: SQLite-based persistence for offline-first capabilities.
- **Remote Data (Supabase)**: Integration with Supabase for cloud synchronization and remote storage.

---

## 📂 Detailed Directory Structure

```text
energy/
├── app/
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/energy/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── AppDatabase.kt          # Room database singleton
│   │   │   │   │   │   ├── UserDao.kt              # Room DAO for User operations
│   │   │   │   │   │   └── TransactionEntity.kt    # Local transaction representation
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── User.kt                 # Primary User data entity
│   │   │   │   │   │   ├── Account.kt              # Energy account data model
│   │   │   │   │   │   └── Transaction.kt          # Transaction data model
│   │   │   │   │   ├── remote/
│   │   │   │   │   │   ├── api/
│   │   │   │   │   │   │   ├── UserApi.kt          # Remote User API definition
│   │   │   │   │   │   │   └── TransactionApi.kt   # Remote Transaction API definition
│   │   │   │   │   │   ├── dto/
│   │   │   │   │   │   │   ├── UserDto.kt          # Remote User Data Transfer Object
│   │   │   │   │   │   │   └── TransactionDto.kt   # Remote Transaction DTO
│   │   │   │   │   │   ├── SupabaseClient.kt       # Supabase connection setup
│   │   │   │   │   │   └── SupabaseConfig.kt       # Remote service configuration
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── UserRepository.kt        # Logic for User data sync/fetch
│   │   │   │   │       ├── AccountRepository.kt     # Logic for Account data sync/fetch
│   │   │   │   │       └── TransactionRepository.kt # Logic for Transaction data sync/fetch
│   │   │   │   ├── ui/theme/
│   │   │   │   │   ├── Color.kt                # Brand color constants
│   │   │   │   │   ├── Theme.kt                # M3 Theme setup
│   │   │   │   │   └── Type.kt                 # Typography configuration
│   │   │   │   ├── AccountScreen.kt            # UI for Account management
│   │   │   │   ├── AuthViewModel.kt            # Manages authentication state & logic
│   │   │   │   ├── BottomMenu.kt               # M3 Bottom Navigation Bar
│   │   │   │   ├── DashboardScreen.kt          # Container for Pager-based navigation
│   │   │   │   ├── HomeScreen.kt               # Landing page (Welcome & Balance)
│   │   │   │   ├── LoginScreen.kt              # Authentication UI
│   │   │   │   ├── MainActivity.kt             # Entry point & Splash management
│   │   │   │   ├── MoreScreen.kt               # Settings & Logout UI
│   │   │   │   ├── TransactionScreen.kt        # History of transactions
│   │   │   │   ├── TransactionViewModel.kt     # Orchestrates transaction sync & UI state
│   │   │   │   └── TransactionViewModelFactory.kt # DI for TransactionViewModel
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   └── ic_energy_logo.xml      # App logo (SVG/Vector)
│   │   │   │   ├── mipmap-anydpi-v26/
│   │   │   │   │   ├── ic_launcher.xml         # Adaptive icon definition
│   │   │   │   │   └── ic_launcher_round.xml   # Round icon definition
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml              # Resource color values
│   │   │   │   │   ├── strings.xml             # App-wide string resources
│   │   │   │   │   └── themes.xml              # Theme and Splash screen styles
│   │   │   │   └── xml/
│   │   │   │       ├── backup_rules.xml
│   │   │   │       └── data_extraction_rules.xml
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build/
├── gradle/
│   └── libs.versions.toml              # Version Catalog for dependencies
├── supabase/
│   ├── migrations/                     # SQL schema definitions
│   └── seed/                           # Initial database records
├── build.gradle.kts                    # Project-level build script
├── settings.gradle.kts                 # Project & Module settings
├── gradlew                             # Gradle wrapper for Unix
└── README.md                           # Project documentation
```

---

## 📄 File Context & Descriptions

### Core UI & Logic
- **[MainActivity.kt](file:///home/frank/AndroidStudioProjects/energy/app/src/main/java/com/example/energy/MainActivity.kt)**: The entry point of the application. It handles the splash screen, edge-to-edge configuration, and hosts the main `DashboardScreen`.
- **[DashboardScreen.kt](file:///home/frank/AndroidStudioProjects/energy/app/src/main/java/com/example/energy/DashboardScreen.kt)**: Acts as the main navigation hub, using a `HorizontalPager` to switch between Home, Transactions, Account, and More screens.
- **[AuthViewModel.kt](file:///home/frank/AndroidStudioProjects/energy/app/src/main/java/com/example/energy/AuthViewModel.kt)**: Handles the business logic for user authentication, interacting with the `UserRepository` to sign up or log in users via Supabase.
- **[TransactionViewModel.kt](file:///home/frank/AndroidStudioProjects/energy/app/src/main/java/com/example/energy/TransactionViewModel.kt)**: Manages the lifecycle of transaction data. It triggers synchronization from the remote server to the local database and exposes the `TransactionUiState` to the UI.

### Data Layer (Local)
- **[AppDatabase.kt](file:///home/frank/AndroidStudioProjects/energy/app/src/main/java/com/example/energy/data/local/AppDatabase.kt)**: Defines the Room database configuration and provides access to DAOs. It implements the Singleton pattern to ensure only one instance of the database is active.
- **[TransactionEntity.kt](file:///home/frank/AndroidStudioProjects/energy/app/src/main/java/com/example/energy/data/local/TransactionEntity.kt)**: The database table definition for transactions, optimized for local storage and mapping to domain models.

### Data Layer (Remote)
- **[SupabaseClient.kt](file:///home/frank/AndroidStudioProjects/energy/app/src/main/java/com/example/energy/data/remote/SupabaseClient.kt)**: Initializes the Supabase SDK with the required plugins (Postgrest for database, GoTrue for Auth).
- **[TransactionApi.kt](file:///home/frank/AndroidStudioProjects/energy/app/src/main/java/com/example/energy/data/remote/api/TransactionApi.kt)**: Handles raw network calls to the Supabase Postgrest endpoint for transaction data.

### Repositories
- **[TransactionRepository.kt](file:///home/frank/AndroidStudioProjects/energy/app/src/main/java/com/example/energy/data/repository/TransactionRepository.kt)**: The orchestrator between local and remote data. It fetches new transactions from `TransactionApi` and persists them into the `AppDatabase`.

---

## 🛠 Tech Stack

- **UI**: Jetpack Compose, Material 3
- **Database**: Room (SQLite) for Local Persistence
- **Networking/Backend**: Supabase (Remote Database & Auth)
- **Architecture**: Layered Architecture with Repository Pattern
- **Threading**: Kotlin Coroutines & Flow
- **Splash Screen**: AndroidX Core Splashscreen API
- **Dependency Management**: Gradle Version Catalog

---

## 🚀 Key Implementation Details

1. **Splash Screen**: Uses `installSplashScreen()` in `MainActivity` to provide a smooth transition from boot to the first frame, configured via `Theme.Energy.Starting`.
2. **Edge-to-Edge**: Implemented via `enableEdgeToEdge()` to ensure UI content draws behind system bars for a modern, immersive look.
3. **Local Persistence**: Uses Room with Coroutines support for non-blocking database operations, ensuring a responsive UI.
4. **Data Synchronization**: The Repository layer handles the coordination between Local (Room) and Remote (Supabase) data sources, acting as the Single Source of Truth.
5. **Declarative Navigation**: Utilizes Compose state and Pagers to handle application flow without complex navigation graphs.

---

## 🧭 Future Roadmap

- [ ] Implement full remote synchronization with Supabase.
- [ ] Add real-time transaction updates using Supabase Realtime.
- [ ] Introduce Dependency Injection using Hilt for better decoupling.
- [ ] Comprehensive Unit Testing for Repository and Data layers.
- [ ] UI Testing using Compose Test Rule and Screenshot Testing.
