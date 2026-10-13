# Connect Dots – Java OOP Project

A fully-featured **Dots and Boxes** game built in Java with a Swing GUI,
implementing strong OOP principles as per the PRD.

---

## Project Structure

```
ConnectDots/
├── src/connectdots/
│   ├── Main.java          – Entry point
│   ├── Player.java        – Encapsulates name, score, colour
│   ├── Line.java          – An edge between two adjacent dots
│   ├── Box.java           – A cell composed of 4 Lines (Composition)
│   ├── MoveRecord.java    – Immutable record for history / undo
│   ├── Board.java         – Grid state: 2D arrays of Lines & Boxes
│   ├── GameLogic.java     – Turn management, scoring, save/load
│   ├── BoardPanel.java    – Swing JPanel – renders board, handles clicks
│   ├── ScorePanel.java    – Live scoreboard header
│   ├── HistoryPanel.java  – Scrollable move-history sidebar
│   ├── NewGameDialog.java – New-game configuration dialog
│   └── GUI.java           – Main JFrame; wires all components
├── build.sh               – Linux/Mac build & run script
├── build.bat              – Windows build & run script
└── README.md
```

---

## OOP Concepts Demonstrated

| Concept | Where |
|---|---|
| **Encapsulation** | `Player`, `Board`, `GameLogic` hide internal state |
| **Abstraction** | `GameLogic.GameEventListener` interface; UI separated from logic |
| **Composition** | `Box` is composed of four `Line` references |
| **Modular design** | Each class has a single responsibility |
| **Serialisation** | Save/Load via `ObjectOutputStream` / `ObjectInputStream` |

---

## Features

- ✅ Dynamic grid (3×3 to 7×7 dots)
- ✅ Two-player local multiplayer
- ✅ Real-time score display
- ✅ Extra turn on box completion
- ✅ Box ownership highlighting with player initials
- ✅ Move history panel (newest first)
- ✅ Undo last move
- ✅ Save / Load game (`.cds` files)
- ✅ New game dialog (custom names + board size)
- ✅ Hover preview of lines
- ✅ Dark-themed modern Swing UI

---

## How to Build & Run

### Linux / macOS
```bash
chmod +x build.sh
./build.sh
```

### Windows
```cmd
build.bat
```

### Manual (any OS)
```bash
# From the ConnectDots directory:
mkdir out
javac -d out src/connectdots/*.java
java -cp out connectdots.Main
```

Requires **Java 11+** (tested on Java 21).

---

## How to Play

1. Click near the gap between two adjacent dots to draw a line.
2. If you complete the 4th side of a box, you score a point and get another turn.
3. The player with the most boxes when the board is full wins!
