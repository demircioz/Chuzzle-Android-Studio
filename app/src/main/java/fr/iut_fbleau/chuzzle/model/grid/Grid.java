package fr.iut_fbleau.chuzzle.model.grid;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;

import fr.iut_fbleau.chuzzle.model.box.Box;

/**
 * The <code>Grid</code> class represents the Chuzzle game board.
 * It is a square grid of {@value #GRID_SIZE}x{@value #GRID_SIZE} cells, each holding
 * a colored {@link Box}. The grid is responsible for initialization, movement,
 * match detection, gravity, and refilling of empty cells.
 *
 * <p>The grid is seeded so that games can be reproduced exactly by reusing the
 * same seed value.</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class Grid {

    /**
     * Number of rows and columns in the grid.
     */
    public static final int GRID_SIZE = 6;

    /**
     * Number of distinct color types available for cells.
     */
    public static final int COLOR_COUNT = 7;

    /**
     * Two-dimensional array of cells making up the board.
     */
    private Box[][] table;

    /**
     * Seed used to initialize the pseudo-random number generator,
     * allowing reproducible game sessions.
     */
    private long seed;

    /**
     * Pseudo-random number generator used for cell color generation
     * and lock mechanics.
     */
    private Random generator;

    /**
     * Actual size of the grid (always equal to {@link #GRID_SIZE}).
     */
    private int size = GRID_SIZE;

    /**
     * Whether gravity is currently inverted (cells fall upward instead of downward).
     * Used by the hard mode.
     */
    private boolean invertedGravity = false;

    /**
     * Physics engine responsible for applying gravity and filling empty cells.
     */
    private GridPhysics physics;

    /**
     * Constructs a new grid with a random seed based on the current time.
     */
    public Grid() {
        this.seed = System.currentTimeMillis();
        this.initGrid();
    }

    /**
     * Constructs a new grid with the given seed, allowing a reproducible game session.
     *
     * @param s the seed for the pseudo-random number generator
     */
    public Grid(long s) {
        this.seed = s;
        this.initGrid();
    }

    /**
     * Initializes the grid by generating a valid board with no initial matches
     * and at least one possible move.
     */
    private void initGrid() {
        this.generator = new Random(this.seed);
        this.generateGridWithoutMatches();
        this.createPhysics();

        GridMoveAnalyst analyst = new GridMoveAnalyst(this.table, this.size);
        while (!analyst.hasPossibleMoves()) {
            this.generateGridWithoutMatches();
            this.createPhysics();
            analyst = new GridMoveAnalyst(this.table, this.size);
        }
    }

    /**
     * Creates a new {@link GridPhysics} instance bound to the current grid state.
     */
    private void createPhysics() {
        this.physics = new GridPhysics(this.table, this.size, this.generator);
    }

    /**
     * Fills the grid with randomly colored cells such that no row or column
     * already contains a sequence of three or more identical colors.
     */
    private void generateGridWithoutMatches() {
        this.table = new Box[this.size][this.size];
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                this.table[i][j] = new Box(i, j, this.generateValidColor(i, j), true);
            }
        }
    }

    /**
     * Generates a color for cell {@code (i, j)} that does not create a horizontal
     * or vertical sequence of three identical colors.
     *
     * @param i the row index of the cell being filled
     * @param j the column index of the cell being filled
     * @return a valid color identifier
     */
    private int generateValidColor(int i, int j) {
        int color;
        boolean valid;
        do {
            color = this.generator.nextInt(COLOR_COUNT);
            valid = true;
            if (j >= 2 && table[i][j-1].getColor() == color && table[i][j-2].getColor() == color) {
                valid = false;
            }
            if (i >= 2 && table[i-1][j].getColor() == color && table[i-2][j].getColor() == color) {
                valid = false;
            }
        } while (!valid);
        return color;
    }

    /**
     * Returns whether at least one valid move exists on the current grid.
     *
     * @return {@code true} if a move that creates a match is possible, {@code false} if the game is over
     */
    public boolean hasPossibleMoves() {
        return new GridMoveAnalyst(this.table, this.size).hasPossibleMoves();
    }

    /**
     * Scans the entire grid for match sequences of three or more identical colors,
     * marks matched cells as empty ({@code color = -1}), and returns the points and
     * series count earned.
     *
     * @return an array of two integers: {@code [totalPoints, seriesCount]}
     */
    public int[] checkMatches() {
        boolean[][] toDestroy = new boolean[this.size][this.size];
        int points = 0, series = 0;

        for (int i = 0; i < this.size; i++) {
            int[] res = scanLine(i, true, toDestroy);
            points += res[0]; series += res[1];
        }
        for (int j = 0; j < this.size; j++) {
            int[] res = scanLine(j, false, toDestroy);
            points += res[0]; series += res[1];
        }

        this.applyDestruction(toDestroy);
        return new int[]{points, series};
    }

    /**
     * Scans a single row or column for match sequences and records the cells
     * to destroy in the provided array.
     *
     * @param idx       the index of the row or column to scan
     * @param isRow     {@code true} to scan a row, {@code false} to scan a column
     * @param toDestroy the destruction map to mark matched cells in
     * @return an array of two integers: {@code [basePoints, seriesCount]} earned from this line
     */
    private int[] scanLine(int idx, boolean isRow, boolean[][] toDestroy) {
        int points = 0, series = 0, j = 0;
        while (j < this.size) {
            int color = isRow ? table[idx][j].getColor() : table[j][idx].getColor();
            if (color == -1) { j++; continue; }
            int len = 1;
            while (j + len < size && (isRow ? table[idx][j+len].getColor() : table[j+len][idx].getColor()) == color) len++;
            if (len >= 3) {
                series++;
                points += calculateBasePoints(len);
                for (int k = 0; k < len; k++) {
                    if (isRow) toDestroy[idx][j+k] = true;
                    else toDestroy[j+k][idx] = true;
                }
            }
            j += len;
        }
        return new int[]{points, series};
    }

    /**
     * Sets the color of all cells marked for destruction to {@code -1} (empty).
     *
     * @param toDestroy the destruction map produced by {@link #scanLine}
     */
    private void applyDestruction(boolean[][] toDestroy) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (toDestroy[i][j]) table[i][j].setColor(-1);
            }
        }
    }

    /**
     * Applies gravity to the grid, causing non-empty cells to fall toward
     * the bottom (or top if gravity is inverted) to fill empty spaces.
     */
    public void applyGravity() {
        this.physics.applyGravity(this.invertedGravity);
    }

    /**
     * Fills all remaining empty cells with new randomly colored cells.
     */
    public void fillEmptySpaces() {
        this.physics.fillEmptySpaces();
    }

    /**
     * Restores the grid to a previously saved state.
     *
     * @param colors          the flat array of color identifiers (row-major order)
     * @param avails          the flat array of availability flags (row-major order)
     * @param generator       the pseudo-random generator state to restore
     * @param invertedGravity the gravity direction to restore
     */
    public void restoreState(int[] colors, boolean[] avails, Random generator, boolean invertedGravity) {
        int index = 0;
        for (int i = 0; i < this.table.length; i++) {
            for (int j = 0; j < this.table[i].length; j++) {
                this.table[i][j].setColor(colors[index]);
                this.table[i][j].setAvailability(avails[index]);
                index++;
            }
        }

        this.generator = generator;
        this.invertedGravity = invertedGravity;
        this.createPhysics();
    }

    /**
     * Returns the base point value for a matched sequence of a given length.
     * Sequences of 3, 4, 5, or 6+ cells are worth 8, 16, 32, and 64 points respectively.
     *
     * @param len the length of the matched sequence
     * @return the base point value for that sequence
     */
    private int calculateBasePoints(int len) {
        if (len == 3) return 8;
        if (len == 4) return 16;
        if (len == 5) return 32;
        return len >= 6 ? 64 : 0;
    }

    /**
     * Resolves all cascading matches triggered by the last move.
     * Repeatedly calls {@link #checkMatches()}, applies gravity, and refills
     * empty cells until no new matches are found.
     *
     * @return an array of two integers: {@code [totalPoints, totalSeries]} across all cascade steps
     */
    public int[] resolveTurnDetails() {
        int pts = 0, series = 0;
        boolean found = true;
        while (found) {
            int[] res = checkMatches();
            if (res[1] > 0) {
                pts += res[0]; series += res[1];
                this.applyGravity();
                this.fillEmptySpaces();
            } else found = false;
        }
        return new int[]{pts, series};
    }

    /**
     * Resolves all cascading matches and returns the final score for the turn,
     * applying the series bonus multiplier.
     *
     * @return the total score earned this turn, or {@code 0} if no match was found
     */
    public int resolveTurn() {
        int[] resolution = this.resolveTurnDetails();
        int points = resolution[0];
        int series = resolution[1];
        return series > 0 ? (int) (points * (1.0 + (series - 1) * 0.5)) : 0;
    }

    /**
     * Shifts row {@code r} one step to the left by {@code d} positions, if the row is not locked.
     *
     * @param r the row index to shift
     * @param d the number of positions to shift
     */
    public void moveLeft(int r, int d) { if (canMoveRow(r)) shift(r, d, true, false); }

    /**
     * Shifts row {@code r} one step to the right by {@code d} positions, if the row is not locked.
     *
     * @param r the row index to shift
     * @param d the number of positions to shift
     */
    public void moveRight(int r, int d) { if (canMoveRow(r)) shift(r, d, true, true); }

    /**
     * Shifts column {@code c} upward by {@code d} positions, if the column is not locked.
     *
     * @param c the column index to shift
     * @param d the number of positions to shift
     */
    public void moveUp(int c, int d) { if (canMoveColumn(c)) shift(c, d, false, false); }

    /**
     * Shifts column {@code c} downward by {@code d} positions, if the column is not locked.
     *
     * @param c the column index to shift
     * @param d the number of positions to shift
     */
    public void moveDown(int c, int d) { if (canMoveColumn(c)) shift(c, d, false, true); }

    /**
     * Performs a circular shift of a row or column by the given distance.
     *
     * @param idx     the row or column index to shift
     * @param dist    the number of single-step shifts to perform
     * @param isRow   {@code true} to shift a row, {@code false} to shift a column
     * @param forward {@code true} to shift right/down, {@code false} to shift left/up
     */
    private void shift(int idx, int dist, boolean isRow, boolean forward) {
        for (int s = 0; s < dist; s++) {
            if (isRow) shiftRow(idx, forward);
            else shiftCol(idx, forward);
        }
    }

    /**
     * Performs a single circular shift of the given row.
     *
     * @param r   the row index to shift
     * @param fwd {@code true} to shift right (elements wrap from the end to the start),
     *            {@code false} to shift left
     */
    private void shiftRow(int r, boolean fwd) {
        int edge = fwd ? size - 1 : 0;
        Box tmp = table[r][edge];
        if (fwd) {
            for (int j = size - 1; j > 0; j--) { table[r][j] = table[r][j-1]; table[r][j].setY(j); }
            table[r][0] = tmp; table[r][0].setY(0);
        } else {
            for (int j = 0; j < size - 1; j++) { table[r][j] = table[r][j+1]; table[r][j].setY(j); }
            table[r][size-1] = tmp; table[r][size-1].setY(size-1);
        }
    }

    /**
     * Performs a single circular shift of the given column.
     *
     * @param c   the column index to shift
     * @param fwd {@code true} to shift downward (elements wrap from the bottom to the top),
     *            {@code false} to shift upward
     */
    private void shiftCol(int c, boolean fwd) {
        int edge = fwd ? size - 1 : 0;
        Box tmp = table[edge][c];
        if (fwd) {
            for (int i = size - 1; i > 0; i--) { table[i][c] = table[i-1][c]; table[i][c].setX(i); }
            table[0][c] = tmp; table[0][c].setX(0);
        } else {
            for (int i = 0; i < size - 1; i++) { table[i][c] = table[i+1][c]; table[i][c].setX(i); }
            table[size-1][c] = tmp; table[size-1][c].setX(size-1);
        }
    }

    /**
     * Returns whether every cell in the given row is available (not locked).
     *
     * @param r the row index to check
     * @return {@code true} if the row can be shifted, {@code false} otherwise
     */
    public boolean canMoveRow(int r) {
        for (int j = 0; j < size; j++) if (!table[r][j].isAvailable()) return false;
        return true;
    }

    /**
     * Returns whether every cell in the given column is available (not locked).
     *
     * @param c the column index to check
     * @return {@code true} if the column can be shifted, {@code false} otherwise
     */
    public boolean canMoveColumn(int c) {
        for (int i = 0; i < size; i++) if (!table[i][c].isAvailable()) return false;
        return true;
    }

    /**
     * Randomly selects one currently available cell and locks it.
     * Does nothing if all cells are already locked.
     */
    public void lockRandomBox() {
        List<Box> free = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) if (table[i][j].isAvailable()) free.add(table[i][j]);
        }
        if (!free.isEmpty()) free.get(generator.nextInt(free.size())).setAvailability(false);
    }

    /**
     * Returns whether a lock event should occur, based on the given probability.
     *
     * @param c the probability of locking a cell (between 0.0 and 1.0)
     * @return {@code true} if a lock should be applied
     */
    public boolean shouldLockBox(double c) { return generator.nextDouble() < c; }

    /**
     * Returns the two-dimensional array of cells making up the grid.
     *
     * @return the grid cell array
     */
    public Box[][] getTable() { return table; }

    /**
     * Returns the seed used to initialize this grid's random generator.
     *
     * @return the seed value
     */
    public long getSeed() { return seed; }

    /**
     * Sets whether gravity is inverted (cells fall upward instead of downward).
     *
     * @param v {@code true} to invert gravity, {@code false} for normal gravity
     */
    public void setInvertedGravity(boolean v) { invertedGravity = v; }

    /**
     * Returns whether gravity is currently inverted.
     *
     * @return {@code true} if gravity is inverted
     */
    public boolean isInvertedGravity() { return invertedGravity; }

    /**
     * Returns the pseudo-random number generator currently used by this grid.
     *
     * @return the random generator
     */
    public Random getGenerator() {
        return this.generator;
    }

    /**
     * Replaces the pseudo-random number generator and recreates the physics engine.
     * Used when restoring a saved game state.
     *
     * @param generator the generator to use
     */
    public void setGenerator(Random generator) {
        this.generator = generator;
        this.createPhysics();
    }
}
