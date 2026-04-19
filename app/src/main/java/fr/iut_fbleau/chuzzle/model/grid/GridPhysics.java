package fr.iut_fbleau.chuzzle.model.grid;

import java.util.Random;

import fr.iut_fbleau.chuzzle.model.box.Box;

/**
 * The <code>GridPhysics</code> class handles the physical simulation of the game grid.
 * It is responsible for applying gravity after cells are destroyed by match sequences,
 * and for refilling the empty cells that result from removals.
 *
 * <p>Gravity can operate in two directions: normal (cells fall downward) or
 * inverted (cells fall upward), as used in hard mode.</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class GridPhysics {

    /**
     * Reference to the grid's cell array, shared with {@link Grid}.
     */
    private Box[][] table;

    /**
     * Size of the grid (number of rows and columns).
     */
    private int size;

    /**
     * Pseudo-random number generator used to assign colors to newly filled cells.
     */
    private Random generator;

    /**
     * Constructs a physics engine bound to the given grid data.
     *
     * @param table     the two-dimensional array of cells to operate on
     * @param size      the number of rows and columns in the grid
     * @param generator the pseudo-random generator used for refilling empty cells
     */
    public GridPhysics(Box[][] table, int size, Random generator) {
        this.table = table;
        this.size = size;
        this.generator = generator;
    }

    /**
     * Applies gravity to all columns of the grid.
     * Empty cells (color {@code -1}) are filled by pulling non-empty cells
     * from above (normal gravity) or from below (inverted gravity).
     *
     * @param inverted {@code true} to apply inverted gravity (cells fall upward),
     *                 {@code false} for normal gravity (cells fall downward)
     */
    public void applyGravity(boolean inverted) {
        if (inverted) {
            this.applyInvertedGravity();
        } else {
            this.applyNormalGravity();
        }
    }

    /**
     * Applies normal (downward) gravity to every column.
     * For each empty cell, pulls the nearest non-empty cell from above down into it.
     */
    private void applyNormalGravity() {
        for (int j = 0; j < this.size; j++) {
            for (int i = this.size - 1; i >= 0; i--) {
                if (this.table[i][j].getColor() == -1) {
                    this.pullDown(i, j);
                }
            }
        }
    }

    /**
     * Pulls the nearest non-empty cell above position {@code (i, j)} down into it.
     *
     * @param i the row of the empty cell
     * @param j the column of the empty cell
     */
    private void pullDown(int i, int j) {
        boolean found = false;
        for (int k = i - 1; k >= 0 && !found; k--) {
            if (this.table[k][j].getColor() != -1) {
                this.swapBoxes(i, j, k, j);
                found = true;
            }
        }
    }

    /**
     * Applies inverted (upward) gravity to every column.
     * For each empty cell, pulls the nearest non-empty cell from below up into it.
     */
    private void applyInvertedGravity() {
        for (int j = 0; j < this.size; j++) {
            for (int i = 0; i < this.size; i++) {
                if (this.table[i][j].getColor() == -1) {
                    this.pullUp(i, j);
                }
            }
        }
    }

    /**
     * Pulls the nearest non-empty cell below position {@code (i, j)} up into it.
     *
     * @param i the row of the empty cell
     * @param j the column of the empty cell
     */
    private void pullUp(int i, int j) {
        boolean found = false;
        for (int k = i + 1; k < this.size && !found; k++) {
            if (this.table[k][j].getColor() != -1) {
                this.swapBoxes(i, j, k, j);
                found = true;
            }
        }
    }

    /**
     * Swaps the contents of two cells and updates their stored position.
     *
     * @param r1 the row of the first cell
     * @param c1 the column of the first cell
     * @param r2 the row of the second cell
     * @param c2 the column of the second cell
     */
    private void swapBoxes(int r1, int c1, int r2, int c2) {
        Box temp = this.table[r1][c1];
        this.table[r1][c1] = this.table[r2][c2];
        this.table[r2][c2] = temp;
        this.table[r1][c1].setX(r1);
        this.table[r1][c1].setY(c1);
        this.table[r2][c2].setX(r2);
        this.table[r2][c2].setY(c2);
    }

    /**
     * Fills every empty cell (color {@code -1}) in the grid with a new randomly
     * colored cell. The filled cells are marked as available.
     */
    public void fillEmptySpaces() {
        for (int i = 0; i < this.size; i++) {
            for (int j = 0; j < this.size; j++) {
                if (this.table[i][j].getColor() == -1) {
                    this.table[i][j].setColor(this.generator.nextInt(Grid.COLOR_COUNT));
                    this.table[i][j].setAvailability(true);
                }
            }
        }
    }
}
