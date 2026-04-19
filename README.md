# 🎮 Chuzzle - Application Android Java (SAE 4.1)

> Un jeu de puzzle-match mobile développé en Java pour Android dans le cadre de la SAE 4.1.

---

## 📖 Sommaire

* [Introduction](#introduction)
* [Fonctionnalités principales](#fonctionnalités-principales)
* [Structure du projet](#structure-du-projet)
* [Installation et lancement](#installation-et-lancement)
* [Documentation](#documentation)
* [Auteurs](#auteurs)
* [Licence](#licence)

---

<a id="introduction"></a>

## 🪶 Introduction

**Chuzzle** est une application Android de réflexion basée sur une grille interactive `6 x 6`.
Le joueur doit faire glisser des lignes ou des colonnes pour former des séries de `3` couleurs identiques ou plus et accumuler le plus de points possible.

Le projet propose plusieurs façons de jouer :

* une **partie normale** générée aléatoirement ;
* une **partie avec seed** pour rejouer une grille précise ;
* une **reprise de partie** grâce à une sauvegarde locale ;
* un **Hard Mode** qui augmente la difficulté avec des verrous et une gravité inversée.

L'application a été conçue avec une structure claire séparant les activités Android, la logique de jeu, la persistance et l'affichage.

---

<a id="fonctionnalités-principales"></a>

## 🚀 Fonctionnalités principales

### 🕹️ Gameplay

* Déplacement circulaire des lignes et colonnes par geste tactile.
* Validation uniquement des coups produisant une combinaison.
* Annulation automatique des mouvements invalides.
* Gestion des cascades, de la gravité et du remplissage de la grille.
* Fin de partie lorsqu'aucun coup valide ne reste.

### 💾 Sauvegarde et rejouabilité

* Sauvegarde locale de la partie en cours.
* Reprise depuis le menu principal.
* Système de **seed** pour relancer une même partie.
* Copie de la seed en fin de partie.

### ⚙️ Interface et options

* Mode **portrait** et **paysage**.
* Chaînes disponibles en **français** et en **anglais**.
* Activation du **Hard Mode** depuis les préférences.

---

<a id="structure-du-projet"></a>

## 🗂️ Structure du projet

```text
SAE41_2025/
├── app/
│   ├── src/main/java/fr/iut_fbleau/chuzzle/
│   │   ├── controller/         # Activités, listeners, logique de jeu, persistance
│   │   ├── model/              # Cases, grille, gravité, analyse des coups
│   │   └── view/               # Rendu graphique du plateau
│   └── src/main/res/           # Layouts, images, chaînes, préférences
├── gradle/
├── res/diagrams/               # Diagrammes PlantUML et SVG
├── Rapport.pdf
└── README.md
```

---

<a id="installation-et-lancement"></a>

## ⚙️ Installation et lancement

### Prérequis

* **Android Studio**
* **JDK 11** ou plus récent
* un **émulateur Android** ou un **appareil physique**

### Lancer le projet

1. Ouvrir le dossier dans Android Studio.
2. Laisser Gradle synchroniser les dépendances.
3. Exécuter l'application sur un émulateur ou un appareil.

### Commandes utiles

Sous Windows :

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

L'APK de debug généré se trouve dans :

```text
app/build/outputs/apk/debug/app-debug.apk
```

---

<a id="documentation"></a>

## 📚 Documentation

Le dépôt contient également :

* `Rapport.pdf` pour la présentation complète du projet ;
* `res/diagrams/` pour les diagrammes ;
* des commentaires Javadoc dans les classes principales.

---

<a id="auteurs"></a>

## 👨‍💻 Auteurs

* Maxime ELIOT
* Canpolat DEMIRCI-OZMEN
* Adrien RABOT

Projet réalisé dans le cadre de la **SAE 4.1**  
**BUT Informatique - IUT de Fontainebleau - UPEC**

---

<a id="licence"></a>

## 📄 Licence

Projet distribué sous licence **MIT**. Voir [LICENSE](LICENSE).
