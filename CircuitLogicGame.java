import java.awt.Color;
import java.util.ArrayList;
import StdDraw;

public class CircuitLogicGame {
    private static final int GRID_WIDTH = 10;     
    private static final int GRID_HEIGHT = 8;     
    private static final double CELL_SIZE = 0.08;
    private static final double MARGIN = 0.05;   

    enum ComponentType {
        EMPTY, BATTERY, WIRE, RESISTOR, LED
    }

    static class Component {
        ComponentType type; // What type of component
        double resistance; // Resistance in Ohms
        boolean isPowered; // Currently receiving electricity?

        Component(ComponentType type) {
            this.type = type;
            this.resistance = 0;
            this.isPowered = false;
        }

        Component(ComponentType type, double resistance) {
            this.type = type;
            this.resistance = resistance;
            this.isPowered = false;
        }
    }

    private Component[][] circuit; // 2D grid storing all components
    private int[] resistors; // Array of 5 resistor values used for sorting
    private boolean[][] powered; // Tracks which cells receive power
    private int score; // Player score from sorting and winning
    private int totalLEDs; // Total LEDs that must be powered to win
    private int poweredLEDs; // Number of LEDs currently powered enough to light
    private boolean gameWon; // Whether the player has already won the game
    private double totalResistance; // Total resistance summed from powered resistors
    private double targetResistance = 40.0; // Maximum resistance allowed to win
    private double currentPowerLevel; // Current power level percentage (0-100)
    private static final double MIN_POWER_FOR_LED = 0.5; // Minimum power ratio for LEDs to light green

    public CircuitLogicGame() {
        circuit = new Component[GRID_HEIGHT][GRID_WIDTH];
        powered = new boolean[GRID_HEIGHT][GRID_WIDTH];
        resistors = new int[5];
        score = 0;
        gameWon = false;

        // Fill grid with empty cells
        for (int i = 0; i < GRID_HEIGHT; i++) {
            for (int j = 0; j < GRID_WIDTH; j++) {
                circuit[i][j] = new Component(ComponentType.EMPTY);
            }
        }

        initializeCircuit();
        
        // Setup graphics: 1000x800 pixels, 0-1 coordinate system
        StdDraw.setCanvasSize(1000, 800);
        StdDraw.setXscale(0, 1);
        StdDraw.setYscale(0, 1);
        StdDraw.enableDoubleBuffering();  // Smooth animation
    }

    private void initializeCircuit() {
        // Place battery at bottom-left corner (row 7, col 0)
        circuit[GRID_HEIGHT - 1][0] = new Component(ComponentType.BATTERY);

        // Place 4 wires connecting battery
        for (int j = 1; j < 5; j++) {
            circuit[GRID_HEIGHT - 1][j] = new Component(ComponentType.WIRE);
        }

        // Place 3 resistors with starting values: 10Ω, 20Ω, 15Ω
        circuit[GRID_HEIGHT - 1][5] = new Component(ComponentType.RESISTOR, 10);
        circuit[GRID_HEIGHT - 1][6] = new Component(ComponentType.RESISTOR, 20);
        circuit[GRID_HEIGHT - 1][7] = new Component(ComponentType.RESISTOR, 15);

        // Initialize resistor array with 5 values for sorting
        resistors[0] = 10;
        resistors[1] = 20;
        resistors[2] = 15;
        resistors[3] = 25;
        resistors[4] = 5;

        // Place 3 random LEDs on grid (top 7 rows only)
        totalLEDs = 0;
        poweredLEDs = 0;
        while (totalLEDs < 3) {
            int row = (int) (Math.random() * (GRID_HEIGHT - 1));
            int col = (int) (Math.random() * GRID_WIDTH);
            // Only place if cell is empty
            if (circuit[row][col].type == ComponentType.EMPTY) {
                circuit[row][col] = new Component(ComponentType.LED);
                totalLEDs++;
            }
        }
    }

    private void traceElectricity() {
        // Reset powered grid - mark all cells unpowered initially
        for (int i = 0; i < GRID_HEIGHT; i++) {
            for (int j = 0; j < GRID_WIDTH; j++) {
                powered[i][j] = false;
            }
        }

        // Start BFS from battery (bottom-left, position [7,0])
        ArrayList<int[]> queue = new ArrayList<>();
        queue.add(new int[]{GRID_HEIGHT - 1, 0});
        powered[GRID_HEIGHT - 1][0] = true;

        // Process each cell and explore neighbors
        int queueIndex = 0;
        while (queueIndex < queue.size()) {
            int[] pos = queue.get(queueIndex++);
            int row = pos[0];
            int col = pos[1];

            // Check all 4 adjacent cells (right, left, down, up)
            int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];

                // If neighbor is valid, unpowered, and not empty
                if (newRow >= 0 && newRow < GRID_HEIGHT && newCol >= 0 && newCol < GRID_WIDTH
                        && !powered[newRow][newCol]) {
                    Component comp = circuit[newRow][newCol];
                    if (comp.type != ComponentType.EMPTY) {
                        powered[newRow][newCol] = true;  // Mark as powered
                        queue.add(new int[]{newRow, newCol});  // Explore its neighbors
                    }
                }
            }
        }

        // Calculate total resistance: sum of all powered resistors
        totalResistance = 0;
        for (int i = 0; i < GRID_HEIGHT; i++) {
            for (int j = 0; j < GRID_WIDTH; j++) {
                if (powered[i][j] && circuit[i][j].type == ComponentType.RESISTOR) {
                    totalResistance += circuit[i][j].resistance;
                }
            }
        }

        // Calculate power level: lower resistance = higher power delivery
        // Formula: Power = 100 / (1 + resistance/50)
        // At 0Ω: 100% | At 40Ω: 71% | At 100Ω: 33%
        currentPowerLevel = 100.0 / (1.0 + totalResistance / 50.0);

        // Count LEDs that are both connected AND have sufficient power (≥50%)
        poweredLEDs = 0;
        for (int i = 0; i < GRID_HEIGHT; i++) {
            for (int j = 0; j < GRID_WIDTH; j++) {
                if (circuit[i][j].type == ComponentType.LED && powered[i][j]) {
                    if (currentPowerLevel >= MIN_POWER_FOR_LED * 100) {
                        poweredLEDs++;
                    }
                }
            }
        }

        // Win condition: ALL LEDs powered AND resistance optimized (≤40Ω)
        if (!gameWon && poweredLEDs == totalLEDs && totalLEDs > 0 && totalResistance <= targetResistance) {
            gameWon = true;
            score += 67;  // Award winning bonus
        }
    }

    /**
     * sortResistors - Sorts resistor array and updates circuit
     * Awards +20 points. Sorting reduces total resistance, improving power delivery.
     */
    private void sortResistors() {
        bubbleSort(resistors);           // Sort in ascending order
        updateCircuitResistors();        // Apply sorted values to circuit
        score += 20;                     // Award sorting bonus
    }

    private void bubbleSort(int[] arr) {
        int n = arr.length;
        // Outer loop: passes through array
        for (int i = 0; i < n - 1; i++) {
            // Inner loop: compare adjacent elements
            for (int j = 0; j < n - i - 1; j++) {
                // If left > right, swap them (move larger values right)
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }

    private void updateCircuitResistors() {
        int resistorIndex = 0;
        // Scan bottom row from left to right
        for (int j = 0; j < GRID_WIDTH; j++) {
            // Find resistor and update its value from sorted array
            if (circuit[GRID_HEIGHT - 1][j].type == ComponentType.RESISTOR && resistorIndex < resistors.length) {
                circuit[GRID_HEIGHT - 1][j].resistance = resistors[resistorIndex++];
            }
        }
    }

    private void handleMouseClick() {
        if (StdDraw.isMousePressed()) {
            // Get mouse positions
            double mouseX = StdDraw.mouseX();
            double mouseY = StdDraw.mouseY();

            // Convert to grid-relative coordinates
            double relativeX = mouseX - MARGIN;
            double relativeY = 1 - MARGIN - mouseY;  // Flip Y (top=1, bottom=0)

            // Convert to grid cell indices (row, col)
            int col = (int) (relativeX / CELL_SIZE);
            int row = (int) (relativeY / CELL_SIZE);

            // Verify click is within grid bounds
            if (row >= 0 && row < GRID_HEIGHT && col >= 0 && col < GRID_WIDTH) {
                // Only place wire if cell is empty
                if (circuit[row][col].type == ComponentType.EMPTY) {
                    circuit[row][col] = new Component(ComponentType.WIRE);
                }
            }
            StdDraw.pause(200);
        }
    }

    private void resetGame() {
        // Reinitialize all data structures
        circuit = new Component[GRID_HEIGHT][GRID_WIDTH];
        powered = new boolean[GRID_HEIGHT][GRID_WIDTH];
        score = 0;
        gameWon = false;

        for (int i = 0; i < GRID_HEIGHT; i++) {
            for (int j = 0; j < GRID_WIDTH; j++) {
                circuit[i][j] = new Component(ComponentType.EMPTY); // Fill grid with empty cells
            }
        }
        initializeCircuit(); // Setup fresh circuit with random LED positions
    }

    private void draw() {
        StdDraw.clear(StdDraw.LIGHT_GRAY);

        for (int i = 0; i < GRID_HEIGHT; i++) {
            for (int j = 0; j < GRID_WIDTH; j++) {
                double x = MARGIN + j * CELL_SIZE + CELL_SIZE / 2;
                double y = 1 - MARGIN - i * CELL_SIZE - CELL_SIZE / 2;

                Component comp = circuit[i][j];

                if (powered[i][j]) {
                    StdDraw.setPenColor(StdDraw.GREEN);
                } else {
                    StdDraw.setPenColor(StdDraw.WHITE);
                }

                switch (comp.type) {
                    case BATTERY:
                        StdDraw.filledRectangle(x, y, CELL_SIZE / 3, CELL_SIZE / 2);
                        StdDraw.setPenColor(StdDraw.BLACK);
                        StdDraw.text(x, y, "+");
                        break;
                    case WIRE:
                        StdDraw.filledRectangle(x, y, CELL_SIZE / 4, CELL_SIZE / 4);
                        break;
                    case RESISTOR:
                        StdDraw.filledRectangle(x, y, CELL_SIZE / 3, CELL_SIZE / 3);
                        StdDraw.setPenColor(StdDraw.BLACK);
                        StdDraw.text(x, y, (int) comp.resistance + "Ω");
                        break;
                    case LED:
                        if (powered[i][j] && currentPowerLevel >= MIN_POWER_FOR_LED * 100) {
                            // Bright green when fully powered
                            StdDraw.setPenColor(StdDraw.GREEN);
                            StdDraw.filledCircle(x, y, CELL_SIZE / 3);
                        } else if (powered[i][j]) {
                            // Dim orange when underpowered
                            StdDraw.setPenColor(new Color(255, 165, 0));
                            StdDraw.filledCircle(x, y, CELL_SIZE / 3);
                        } else {
                            // Red when not connected
                            StdDraw.setPenColor(StdDraw.RED);
                            StdDraw.filledCircle(x, y, CELL_SIZE / 3);
                        }
                        break;
                    case EMPTY:
                        StdDraw.setPenColor(StdDraw.LIGHT_GRAY);
                        StdDraw.filledRectangle(x, y, CELL_SIZE / 2, CELL_SIZE / 2);
                        break;
                }
            }
        }

        // Draw UI Panel
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.text(0.85, 0.95, "CIRCUIT BUILDER");
        StdDraw.text(0.15, 0.95, "Score: " + score);
        
        // Objective
        StdDraw.text(0.5, 0.92, "Objective: Power all " + totalLEDs + " LEDs + Optimize Resistance");
        StdDraw.text(0.5, 0.88, "LEDs: " + poweredLEDs + "/" + totalLEDs + " | Resistance: " + String.format("%.1f", totalResistance) + "Ω / Target: " + (int)targetResistance + "Ω");
        StdDraw.text(0.5, 0.84, "Power Level: " + String.format("%.1f", currentPowerLevel) + "%");

        // Win State
        if (gameWon) {
            StdDraw.setPenColor(Color.GREEN);
            StdDraw.text(0.5, 0.80, "★ YOU WIN! ★ +67 BONUS!");
            StdDraw.setPenColor(StdDraw.BLACK);
        } else if (poweredLEDs == totalLEDs && totalLEDs > 0 && totalResistance > targetResistance) {
            StdDraw.setPenColor(Color.ORANGE);
            StdDraw.text(0.5, 0.80, "Almost there! Sort to reduce resistance.");
            StdDraw.setPenColor(StdDraw.BLACK);
        }
        // Instructions
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.text(0.5, 0.06, "Click to place wires | S: Sort Resistors | R: Reset");
        
        StdDraw.show();
    }

    /**
     * gameLoop - Main game loop: updates game state, handles input, renders each frame
     * Updates electricity, handles clicks/keys, and renders graphics
     */
    private void gameLoop() {
        while (true) {
            traceElectricity();      // Calculate power delivery and check win condition
            handleMouseClick();      // Check for wire placement clicks
            draw();                  // Render all graphics

            //Handle keyboard input
            if (!gameWon && StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                if (key == 's' || key == 'S') {
                    sortResistors();
                } else if (key == 'r' || key == 'R') {
                    resetGame();
                }
            } else if (gameWon && StdDraw.hasNextKeyTyped()) {
                //After winning: only R=reset is allowed (game is frozen)
                char key = StdDraw.nextKeyTyped();
                if (key == 'r' || key == 'R') {
                    resetGame();
                }
            }
            StdDraw.pause(50);
        }
    }
    public static void main(String[] args) {
        CircuitLogicGame game = new CircuitLogicGame();  // Create and initialize game
        game.gameLoop();                                  // Start main loop
    }
}
