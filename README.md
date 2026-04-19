# 🎮 Chuzzle - Android Java Application (SAE 4.1)

> A mobile puzzle-match game developed in Java for Android as part of SAE 4.1.

---

## 📖 Table of Contents

* [Introduction](#introduction)
* [Main Features](#main-features)
* [Project Structure](#project-structure)
* [Installation and Launch](#installation-and-launch)
* [Documentation](#documentation)
* [Authors](#authors)
* [License](#license)

---

<a id="introduction"></a>

## 🪶 Introduction

**Chuzzle** is an Android puzzle game based on an interactive `6 x 6` grid.  
The player must slide rows or columns to create matches of `3` identical colors or more and score as many points as possible. :contentReference[oaicite:0]{index=0}

The project offers several ways to play:

* a **normal game** generated randomly;
* a **seeded game** to replay a specific grid;
* a **resume game** feature thanks to local save data;
* a **Hard Mode** that increases difficulty with locks and reversed gravity. :contentReference[oaicite:1]{index=1}

The application was designed with a clear structure separating Android activities, game logic, persistence, and rendering. :contentReference[oaicite:2]{index=2}

---

<a id="main-features"></a>

## 🚀 Main Features

### 🕹️ Gameplay

* Circular movement of rows and columns through touch gestures.
* Validation only for moves that create a combination.
* Automatic cancellation of invalid moves.
* Management of cascades, gravity, and board refilling.
* Game over when no valid move remains. :contentReference[oaicite:3]{index=3}

### 💾 Save System & Replayability

* Local save of the current game.
* Resume from the main menu.
* **Seed** system to restart the same game.
* Copy the seed at the end of the game. :contentReference[oaicite:4]{index=4}

### ⚙️ Interface & Options

* **Portrait** and **landscape** modes.
* Available strings in **French** and **English**.
* Enable **Hard Mode** from preferences. :contentReference[oaicite:5]{index=5}

---

<a id="project-structure"></a>

## 🗂️ Project Structure

```text
SAE41_2025/
├── app/
│   ├── src/main/java/fr/iut_fbleau/chuzzle/
│   │   ├── controller/         # Activities, listeners, game logic, persistence
│   │   ├── model/              # Cells, grid, gravity, move analysis
│   │   └── view/               # Board graphic rendering
│   └── src/main/res/           # Layouts, images, strings, preferences
├── gradle/
├── res/diagrams/               # PlantUML and SVG diagrams
├── Rapport.pdf
└── README.md
````



---

<a id="installation-and-launch"></a>

## ⚙️ Installation and Launch

### Prerequisites

* **Android Studio**
* **JDK 11** or newer
* an **Android emulator** or a **physical device** 

### Run the Project

1. Open the folder in Android Studio.
2. Let Gradle sync dependencies.
3. Run the application on an emulator or device. 

### Useful Commands

On Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

The generated debug APK is located in:

```text
app/build/outputs/apk/debug/app-debug.apk
```



---

<a id="documentation"></a>

## 📚 Documentation

The repository also contains:

* `Rapport SAé Chuzzle - Semestre 4 - Android Studio - Java.pdf` for the complete project presentation (in French);
* `res/diagrams/` for diagrams;
* Javadoc comments in the main classes. 

---

<a id="authors"></a>

## 👨‍💻 Authors

* Maxime ELIOT
* Canpolat DEMIRCI-OZMEN
* Adrien RABOT

Project completed as part of **SAE 4.1**
**BUT Computer Science - IUT of Fontainebleau - UPEC** 

---

<a id="license"></a>

## 📄 License

Project distributed under the **MIT** license. See [LICENSE](LICENSE). 