# Java Chess Engine with Playable Bot

## Project Overview
This repository contains the source code for a fully functional Chess Engine developed in Java. The application features a graphical user interface (GUI) and a single player mode against an Artificial Intelligence (AI) bot. 

The project was developed as a group assignment to demonstrate proficiency in Object Oriented Programming (OOP), software architecture, and algorithmic problem solving. Unlike standard chess libraries, this engine implements the board representation, move validation logic, and search algorithms entirely from scratch.

## Table of Contents
1. [Key Features](#key-features)
2. [System Architecture](#system-architecture)
3. [The Playable Bot](#the-playable-bot)
4. [Prerequisites](#prerequisites)
5. [Installation and Execution](#installation-and-execution)
6. [Project Team](#project-team)
7. [Acknowledgements](#acknowledgements)

---

## Key Features

### Core Gameplay
*   **Complete Ruleset:** Supports all standard FIDE chess rules, including complex mechanics such as Castling (Kingside and Queenside), En Passant captures, and Pawn Promotion.
*   **Game State Detection:** Automatically detects and announces Check, Checkmate, Stalemate, and Draw conditions.
*   **Move Validation:** A strict validation engine prevents illegal moves, ensuring the integrity of the game state.

### User Interface
*   **Interactive Board:** Drag-and-drop functionality for moving pieces.
*   **Visual Feedback:** Highlights selected pieces and valid move destinations.

---

## System Architecture

The application is built using the **Model-View-Controller (MVC)** architectural pattern to ensure separation of concerns and maintainability.

### 1. Model (Logic Layer)
This layer handles the internal data structures.
*   **Board Representation:** The board is stored as an 8x8 2D array of `Piece` objects.
*   **Piece Logic:** An abstract `Piece` class defines common attributes, while concrete classes (`Rook`, `Bishop`, `Knight`, etc.) implement specific movement vectors.

### 2. View (Presentation Layer)
This layer manages the Graphical User Interface.
*   Built using **Java Swing/AWT**.
*   Utilizes double-buffering to render the board smoothly without flickering.
*   Updates the display only when notified of changes by the Controller.

### 3. Controller (Input Layer)
This layer bridges the gap between the user and the logic.
*   Captures mouse events (clicks and drags).
*   Translates pixel coordinates into grid coordinates (Rank and File).
*   Calls the Model to validate and execute moves.

---

## The Playable Bot

The computer opponent is powered by a custom implementation of the **Minimax Algorithm** with **Alpha-Beta Pruning**.

*   **Search Depth:** The AI looks ahead a configurable number of moves (default is depth 3) to predict the best outcome.
*   **Evaluation Function:** The AI evaluates board states based on:
    *   **Material:** The combined point value of surviving pieces.
    *   **Position:** Piece Square Tables are used to encourage the AI to control the center of the board and develop pieces early.
*   **Optimization:** Alpha-Beta pruning is utilized to eliminate search branches that cannot influence the final decision, significantly reducing calculation time.

---

## Prerequisites

To run this application, ensure your system meets the following requirements:

*   **Java Development Kit (JDK):** Version 11 or higher.
*   **Operating System:** Windows, macOS, or Linux.
*   **IDE (Optional):** IntelliJ IDEA, Eclipse, or NetBeans for editing the code.

---

## Installation and Execution

Follow these steps to set up the project on your local machine.

### Step 1: Clone the Repository
Open your terminal or command prompt and run:
```
git clone https://github.com/Noobovich1/DSA-Project-Chess-CodeStrike.git
```

### Step 2: Open the Project
Using an IDE: Open your IDE (e.g., IntelliJ IDEA) and select "Open" or "Import Project." Navigate to the cloned folder.

Using Command Line: Navigate to the src directory.


### Step 3: Compile the Code
If you are not using an IDE, compile the Java files from the root src directory:

```
javac src/main/main.java
```

### Step 4: Run the Application
Execute the main class to start the game:
```
java src.main.main
```

---

## Project Team
This project was a collaborative effort by the following members:

**[Nguyễn Hưng]:** Working on the Artificial Intelligence, implementing the Minimax algorithm and board evaluation strategies.

**[Nguyễn Thế Khoa]:** Handling the Object Oriented Design of the pieces and enforced the complex rules of Chess like Castling and En Passant.

**[Nguyễn Dương Đức Thịnh]:**	Focusing on the User Interface and Experience, handling graphics, assets, and input event listeners.

**[Phạm Gia Thịnh]:** Acting as the System Integrator, managing the game loop, sound, game states, and win/loss condition logic.

*P/S: While specific roles were assigned, every member actively contributed across all modules to ensure seamless system integration.*

---

## Acknowledgements

[Dr. Trần Thanh Tùng & Mr. Đặng Tâm Nhân]: For guidance on software design patterns.

Chess Programming Wiki: For technical documentation on move generation algorithms.

Open Source Assets: Chess piece images were sourced from [Chess.com].
