# fittracker

# FitTrack Pro - Calorie & Macro Tracker for Gym, Athletes & Fitness

A modern, offline-first Android Calorie, Macro, and Fitness Tracker application built with **Jetpack Compose**, **Kotlin Coroutines & Flow**, and **Room Database**.

Designed specifically for bodybuilders, athletes, sports enthusiasts, and anyone looking to manage their nutrition, hit calorie goals, calculate BMI, and build sustainable healthy habits.

---

## Key Features

- **Personalized Goals & Fitness Personas**:
  - Profiles for **Gym Guy / Bodybuilding**, **Sports Person / Athlete**, and **Everyday Health & Fitness**.
  - Dynamic BMR & TDEE calculation with macro breakdown (Protein, Carbs, Fats) based on weight goals (Cut, Maintain, Bulk).
  - Accurate BMI calculation & classification.
- **Smart Calorie & Macro Tracking**:
  - Real-time interactive calorie progress ring with Base Goal, Food Eaten, and Exercise Burn calculations.
  - Responsive macro progress bars with target zone status indicator (`● On Track`).
  - Pre-loaded database of 150+ foods including Indian cuisine, high-protein gym foods, snacks, and staples.
  - Quick Re-log for frequent foods and Quick Add with smart macro estimation.
- **Workout & Water Tracking**:
  - Calorie burn logging across gym lifting, cardio, sports, and daily activities.
  - Smart hydration tracker with `+250ml`, `+500ml`, and `-250ml` quick-adjust buttons.
- **Streaks & Progress Analytics**:
  - Automated daily meal logging streak tracker (`🔥 Streak Banner`).
  - Weekly calorie intake bar charts and 7-day rolling macro averages (Protein, Carbs, Fat).
  - Weekly Adherence scoring (`X/7 Days 🎯`).
- **Data Safety & Offline First**:
  - 100% offline Room database with indexed tables and zero data loss migrations.
  - 1-tap Fitness Data Summary export to clipboard.
  - Instant Undo support for deleted meals.

---

## Tech Stack & Architecture

- **UI**: 100% Jetpack Compose with Material 3 & custom dark athletic theme
- **Architecture**: MVVM with unidirectional data flow (StateFlow & SharedFlow)
- **Database**: Room Database (SQLite) with schema migrations and indexed queries
- **Concurrency**: Kotlin Coroutines on `Dispatchers.IO`
- **Language**: Kotlin 2.0+
- **Min SDK**: Android 8.0 (API 26) / Target SDK: Android 14+ (API 34)

---

## Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- Android SDK 34+
- JDK 17+

### Build & Run
```bash
# Clone the repository
git clone https://github.com/md-musharraf/fittracker.git

# Navigate into the project
cd fittracker

# Build the debug APK
./gradlew assembleDebug

# Install on a connected Android device
./gradlew installDebug
```
