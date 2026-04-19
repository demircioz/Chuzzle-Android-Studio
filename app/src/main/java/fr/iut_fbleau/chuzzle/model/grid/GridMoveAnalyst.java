package fr.iut_fbleau.chuzzle.model.grid;

import fr.iut_fbleau.chuzzle.model.box.Box;

/**
 * The <code>GridMoveAnalyst</code> class analyses the current state of the grid
 * to determine whether at least one valid move remains available to the player.
 *
 * <p>A move is considered valid if shifting a row or column by any amount
 * (from 1 to {@code size - 1} positions) would create a horizontal or vertical
 * sequence of three or more identical colors. The analysis uses virtual circular
 * shifting to avoid modifying the actual grid.</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class GridMoveAnalyst {

    /**
     * Reference to the grid's cell array, used read-only during analysis.
     */
    private Box[][] table;

    /**
     * Size of the grid (number of rows and columns).
     */
    private int size;

    /**
     * Constructs an analyst for the given grid snapshot.
     *
     * @param table the two-dimensional array of cells to analyse
     * @param size  the number of rows and columns in the grid
     */
    public GridMoveAnalyst(Box[][] table, int size) {
        this.table = table;
        this.size = size;
    }

    /**
     * Returns whether at least one valid move exists on the current grid.
     * Checks all unlocked rows and columns for shifts that would create a match.
     *
     * @return {@code true} if a valid move is possible, {@code false} if the game is over
     */
    public boolean hasPossibleMoves() {
        for (int row = 0; row < this.size; row++) {
            if (this.canMoveRow(row) && this.hasPossibleRowShift(row)) {
                return true;
            }
        }
        for (int col = 0; col < this.size; col++) {
            if (this.canMoveColumn(col) && this.hasPossibleColumnShift(col)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether every cell in the given row is available (not locked).
     *
     * @param rowIndex the row index to check
     * @return {@code true} if the row can be shifted
     */
    private boolean canMoveRow(int rowIndex) {
        for (int j = 0; j < this.size; j++) {
            if (!this.table[rowIndex][j].isAvailable()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether every cell in the given column is available (not locked).
     *
     * @param colIndex the column index to check
     * @return {@code true} if the column can be shifted
     */
    private boolean canMoveColumn(int colIndex) {
        for (int i = 0; i < this.size; i++) {
            if (!this.table[i][colIndex].isAvailable()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether any right-shift of the given row by 1 to {@code size - 1}
     * positions would create a match sequence.
     *
     * @param row the row index to test
     * @return {@code true} if at least one shift creates a match
     */
    private boolean hasPossibleRowShift(int row) {
        for (int shift = 1; shift < this.size; shift++) {
            if (this.createsMatchAfterRowShift(row, shift)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether any downward shift of the given column by 1 to {@code size - 1}
     * positions would create a match sequence.
     *
     * @param col the column index to test
     * @return {@code true} if at least one shift creates a match
     */
    private boolean hasPossibleColumnShift(int col) {
        for (int shift = 1; shift < this.size; shift++) {
            if (this.createsMatchAfterColumnShift(col, shift)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether a right-shift of the given row by {@code shift} positions
     * would create at least one match sequence.
     *
     * @param row   the row index to test
     * @param shift the number of positions to shift right
     * @return {@code true} if the shift would create a match
     */
    private boolean createsMatchAfterRowShift(int row, int shift) {
        for (int col = 0; col < this.size; col++) {
            int color = this.getRowColorAfterRightShift(row, col, shift);
            if (color != -1) {
                if (this.hasHorizontalMatchOnShiftedRow(row, col, shift, color)
                        || this.hasVerticalMatchAt(row, col, color)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether a downward shift of the given column by {@code shift} positions
     * would create at least one match sequence.
     *
     * @param col   the column index to test
     * @param shift the number of positions to shift down
     * @return {@code true} if the shift would create a match
     */
    private boolean createsMatchAfterColumnShift(int col, int shift) {
        for (int row = 0; row < this.size; row++) {
            int color = this.getColumnColorAfterDownShift(col, row, shift);
            if (color != -1) {
                if (this.hasVerticalMatchOnShiftedColumn(col, row, shift, color)
                        || this.hasHorizontalMatchAt(row, col, color)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether a sequence of three or more identical colors exists
     * horizontally in the shifted row around position {@code col}.
     *
     * @param row   the row being analysed
     * @param col   the column position to check around
     * @param shift the right-shift amount applied to the row
     * @param color the color to match
     * @return {@code true} if a horizontal match of 3+ exists
     */
    private boolean hasHorizontalMatchOnShiftedRow(int row, int col, int shift, int color) {
        int count = 1;
        for (int c = col - 1; c >= 0 && this.getRowColorAfterRightShift(row, c, shift) == color; c--) {
            count++;
        }
        for (int c = col + 1; c < this.size && this.getRowColorAfterRightShift(row, c, shift) == color; c++) {
            count++;
        }
        return count >= 3;
    }

    /**
     * Returns whether a sequence of three or more identical colors exists
     * vertically in the shifted column around position {@code row}.
     *
     * @param col   the column being analysed
     * @param row   the row position to check around
     * @param shift the downward-shift amount applied to the column
     * @param color the color to match
     * @return {@code true} if a vertical match of 3+ exists
     */
    private boolean hasVerticalMatchOnShiftedColumn(int col, int row, int shift, int color) {
        int count = 1;
        for (int r = row - 1; r >= 0 && this.getColumnColorAfterDownShift(col, r, shift) == color; r--) {
            count++;
        }
        for (int r = row + 1; r < this.size && this.getColumnColorAfterDownShift(col, r, shift) == color; r++) {
            count++;
        }
        return count >= 3;
    }

    /**
     * Returns whether a sequence of three or more identical colors exists
     * horizontally in the current (unshifted) grid around position {@code (row, col)}.
     *
     * @param row   the row to check
     * @param col   the column position to check around
     * @param color the color to match
     * @return {@code true} if a horizontal match of 3+ exists
     */
    private boolean hasHorizontalMatchAt(int row, int col, int color) {
        int count = 1;
        for (int c = col - 1; c >= 0 && this.table[row][c].getColor() == color; c--) {
            count++;
        }
        for (int c = col + 1; c < this.size && this.table[row][c].getColor() == color; c++) {
            count++;
        }
        return count >= 3;
    }

    /**
     * Returns whether a sequence of three or more identical colors exists
     * vertically in the current (unshifted) grid around position {@code (row, col)}.
     *
     * @param row   the row position to check around
     * @param col   the column to check
     * @param color the color to match
     * @return {@code true} if a vertical match of 3+ exists
     */
    private boolean hasVerticalMatchAt(int row, int col, int color) {
        int count = 1;
        for (int r = row - 1; r >= 0 && this.table[r][col].getColor() == color; r--) {
            count++;
        }
        for (int r = row + 1; r < this.size && this.table[r][col].getColor() == color; r++) {
            count++;
        }
        return count >= 3;
    }

    /**
     * Returns the color that would appear at column {@code targetCol} in the given row
     * after a right-shift of {@code shift} positions (using circular wrapping).
     *
     * @param row       the row being shifted
     * @param targetCol the column position to query in the shifted state
     * @param shift     the number of positions shifted to the right
     * @return the color at the queried position after the virtual shift
     */
    private int getRowColorAfterRightShift(int row, int targetCol, int shift) {
        int sourceCol = this.wrapIndex(targetCol - shift);
        return this.table[row][sourceCol].getColor();
    }

    /**
     * Returns the color that would appear at row {@code targetRow} in the given column
     * after a downward shift of {@code shift} positions (using circular wrapping).
     *
     * @param col       the column being shifted
     * @param targetRow the row position to query in the shifted state
     * @param shift     the number of positions shifted downward
     * @return the color at the queried position after the virtual shift
     */
    private int getColumnColorAfterDownShift(int col, int targetRow, int shift) {
        int sourceRow = this.wrapIndex(targetRow - shift);
        return this.table[sourceRow][col].getColor();
    }

    /**
     * Wraps a potentially negative or out-of-bounds index into the valid range
     * {@code [0, size - 1]} using modular arithmetic.
     *
     * @param index the raw index to wrap
     * @return the wrapped index in the range {@code [0, size - 1]}
     */
    private int wrapIndex(int index) {
        int wrapped = index % this.size;
        return wrapped < 0 ? wrapped + this.size : wrapped;
    }
}
