# Circuit Logic Game

## Overview
**Circuit Logic Game** is an interactive Java puzzle game where players build electrical circuits on a 2D grid by routing wires from a battery to power LEDs, while optimizing circuit resistance using sorting algorithms. The game combines spatial reasoning, algorithmic thinking, and real-time physics simulation to create an engaging educational experience.

## Table of Contents
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Game Mechanics](#game-mechanics)
- [Project Structure](#project-structure)
- [Technologies](#technologies)
- [Contributing](#contributing)
- [License](#license)

## Features
- **Electrical Pathfinding**: BFS (Breadth-First Search) algorithm traces power delivery from battery through wire networks in real-time
- **Dynamic Power Calculation**: Real-time resistance optimization using Ohm's Law approximation formula
- **Interactive Puzzle Solving**: Players must connect all LEDs and reduce total resistance below 40Ω to win
- **Click-Based Wire Placement**: Intuitive grid system for designing custom circuits
- **Sorting Optimization**: Bubble sort algorithm reorganizes resistor values to minimize total resistance
- **Score System**: Points awarded for sorting (+20) and winning (+67)
- **Visual Feedback**: Color-coded components (green = powered, orange = underpowered, red = unpowered)
- **Clean, Maintainable Code Architecture**: Well-organized with enum types and clear separation of concerns
- **Comprehensive Error Handling**: Boundary checking and state validation throughout game loop

## Prerequisites
Before running this project, ensure you have the following installed:
- **Java Development Kit (JDK)**: Version 11 or higher
- **StdDraw Library**: Graphics library for rendering (included in project)
- **Git**: For version control

## Installation

### 1. Clone the Repository
```bash
git clone https://github.com/startech8965/JavaProject.git
cd JavaProject
```

### 2. Compile the Project
```bash
javac CircuitLogicGame.java
```

### 3. Run the Application
```bash
java CircuitLogicGame
```

## Usage

### Basic Gameplay
1. **Place Wires**: Click on empty grid cells to place wire segments connecting the battery (bottom-left) to the LEDs (randomly placed)
2. **Monitor Power**: Watch the power level percentage and LED colors in real-time as your circuit grows
3. **Sort Resistors**: Press `S` to sort resistors in ascending order, reducing total resistance and increasing power delivery
4. **Win Condition**: Light up all LEDs (turn bright green) AND reduce total resistance to ≤40Ω
5. **Reset**: Press `R` to clear the board and start a new game

### Game Controls
- **Mouse Click**: Place wires on empty cells
- **S Key**: Sort resistor values using bubble sort algorithm
- **R Key**: Reset game and generate new random LED positions
- **Close Window**: Exit the game

### Configuration
- **Grid Dimensions**: 10×8 (adjustable via `GRID_WIDTH` and `GRID_HEIGHT`)
- **Target Resistance**: 40Ω (adjustable via `targetResistance` constant)
- **Minimum Power for LED**: 50% (adjustable via `MIN_POWER_FOR_LED` constant)
- **Cell Size**: 0.08 units (adjustable via `CELL_SIZE` constant)

## Game Mechanics

### Circuit Components
- **Battery**: Fixed power source at bottom-left corner; always powered
- **Wires**: Conductive paths placed by the player; carry power if connected to battery
- **Resistors**: Components in bottom row that reduce power; their order affects total resistance
- **LEDs**: Targets placed randomly; glow green when powered AND power level ≥50%

### Power Delivery System
The game uses a **Breadth-First Search (BFS)** algorithm to trace electricity:

```
1. Start from battery position (row 7, col 0)
2. Mark battery as powered
3. Explore all adjacent cells (up, down, left, right)
4. If neighbor contains a component and isn't powered yet, mark as powered and continue
5. Repeat until all reachable components are explored
```

### Resistance Calculation
Total Resistance = Sum of all resistances in powered resistor cells

Power Delivery = 100 / (1 + totalResistance / 50)

Example: 
- 0Ω → 100% power
- 40Ω → 71% power  
- 100Ω → 33% power

### Win Condition
- ALL LEDs must be powered (connected to battery via wires)
- ALL LEDs must receive sufficient power (≥50% power level)
- Total resistance must be ≤40Ω

## Project Structure
```

### Key Classes & Methods

**CircuitLogicGame.java**
- `gameLoop()`: Main loop updating logic, handling input, rendering (50ms per frame)
- `traceElectricity()`: BFS pathfinding algorithm; calculates powered cells and power levels
- `sortResistors()`: Initiates bubble sort and updates circuit
- `bubbleSort(int[] arr)`: Standard bubble sort implementation
- `handleMouseClick()`: Detects clicks and places wires
- `draw()`: Renders grid, components, and UI
- `resetGame()`: Reinitializes all data structures for new game
- `initializeCircuit()`: Sets up battery, resistors, and random LEDs

## Technologies
- **Java**: Object-oriented programming language
- **StdDraw**: Graphics library for 2D rendering
- **Algorithms**: 
  - BFS (Breadth-First Search) for pathfinding
  - Bubble Sort for optimization
- **Data Structures**: 
  - 2D Arrays for grid representation
  - ArrayLists for queue operations
- **Graphics**: Double-buffering for smooth animation

## Contributing
Contributions are welcome! To contribute:

1. Fork the repository
2. Create a new branch (`git checkout -b feature/YourFeature`)
3. Make your changes and test thoroughly
4. Commit your changes (`git commit -m 'Add YourFeature'`)
5. Push to the branch (`git push origin feature/YourFeature`)
6. Open a Pull Request describing your improvements

### Potential Enhancement Ideas
- Add difficulty levels (more LEDs, stricter resistance targets)
- Implement different component types (capacitors, switches)
- Add a timer mode for speed challenges
- Create a level editor or pre-built circuit puzzles
- Add sound effects and visual animations
- Implement mobile/touch support

## License
This project is licensed under the MIT License - see the LICENSE file for details.

---

**Author**: startech8965  
**Last Updated**: June 1, 2026  
**Project Type**: Educational Puzzle Game  
**Status**: Complete and Playable
