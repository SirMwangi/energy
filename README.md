# Energy Android App

Energy is a modern Android application built using **Jetpack Compose** and **Kotlin**. It provides a secure platform for users to manage their energy-related accounts and transactions, featuring a smooth user experience with local data persistence and a dedicated splash screen.

## Features

- **User Authentication**: Secure Sign Up and Log In functionality using a local Room database.
- **Splash Screen**: Professional entry with a custom logo using the AndroidX Splashscreen API.
- **Dashboard Navigation**: Seamless switching between Home, Accounts, Transactions, and More screens using a bottom navigation bar and a horizontal pager.
- **Local Persistence**: Reliable data storage for user accounts powered by SQLite and Room.
- **Modern UI/UX**: Fully declarative UI built with Material 3 design principles.

## Project Structure and File Descriptions

### Root Directory
- `build.gradle.kts`: Root-level build configuration.
- `settings.gradle.kts`: Project settings and module definitions.
- `gradle.properties`: Project-wide Gradle settings.
- `gradlew` / `gradlew.bat`: Gradle wrapper scripts.
- `README.md`: This documentation file.

### App Module (`/app`)
- `build.gradle.kts`: Module-specific build configuration, dependencies, and Android settings.
- `src/main/AndroidManifest.xml`: Essential app information including components and permissions.

#### Java/Kotlin Source (`app/src/main/java/com/example/energy`)
- **Main Components**:
    - `MainActivity.kt`: The main entry point. Sets up the splash screen and manages the high-level navigation flow (Sign Up -> Login -> Dashboard).
    - `DashboardScreen.kt`: Manages the post-login experience, coordinating the horizontal pager and the bottom menu.
    - `BottomMenu.kt`: A custom implementation of the Material 3 Navigation Bar.
- **Screens**:
    - `HomeScreen.kt`: Greets the user and displays their current balance.
    - `AccountScreen.kt`: Displays a list of user accounts.
    - `TransactionScreen.kt`: Shows the user's transaction history.
    - `MoreScreen.kt`: Contains additional settings and the logout option.
    - `LoginScreen.kt`: Handles user authentication logic and UI.
- **Data Layer (`/data`)**:
    - `user.kt`: Defines the `User` entity for the Room database.
    - `UserDao.kt`: Interface for database operations like inserting and retrieving users.
    - `AppDatabase.kt`: The Room database class that serves as the main access point for the underlying SQLite database.
- **UI Theme (`/ui/theme`)**:
    - `Theme.kt`: Defines the Material 3 theme colors and application theme.
    - `Color.kt`: Specific color constants for the app.
    - `Type.kt`: Typography settings for the application.

#### Resources (`app/src/main/res`)
- `drawable/ic_energy_logo.xml`: Custom vector logo used for the splash screen and launcher icon.
- `values/themes.xml`: XML theme definitions, including the specialized splash screen theme (`Theme.Energy.Starting`).
- `values/colors.xml`: Color definitions for the UI and system bars.
- `values/strings.xml`: Application string resources for internationalization.
- `mipmap-*/`: App launcher icon assets for different screen densities.
- `xml/`: Configuration for backup and data extraction rules.

## Directory Structure

```text
energy/
├── app/
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/energy/
│   │   │   │   ├── data/
│   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   ├── UserDao.kt
│   │   │   │   │   └── user.kt
│   │   │   │   ├── ui/theme/
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   └── Type.kt
│   │   │   │   ├── AccountScreen.kt
│   │   │   │   ├── BottomMenu.kt
│   │   │   │   ├── DashboardScreen.kt
│   │   │   │   ├── HomeScreen.kt
│   │   │   │   ├── LoginScreen.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── MoreScreen.kt
│   │   │   │   └── TransactionScreen.kt
│   │   │   ├── res/
│   │   │   │   ├── drawable/
│   │   │   │   │   └── ic_energy_logo.xml
│   │   │   │   ├── mipmap-anydpi-v26/
│   │   │   │   │   ├── ic_launcher.xml
│   │   │   │   │   └── ic_launcher_round.xml
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── xml/
│   │   │   │       ├── backup_rules.xml
│   │   │   │       └── data_extraction_rules.xml
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build/
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
└── README.md
```

## Getting Started

1.  Clone the repository.
2.  Open the project in **Android Studio**.
3.  Sync the project with Gradle files.
4.  Run the application on an emulator or a physical device.
