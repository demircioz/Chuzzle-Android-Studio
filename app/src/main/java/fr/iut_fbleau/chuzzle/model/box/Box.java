package fr.iut_fbleau.chuzzle.model.box;

/**
 * The <code>Box</code> class represents a single cell on the game grid.
 * Each cell has a position in the grid, a color identifier, and an
 * availability state indicating whether it is locked or not.
 *
 * <p>A color value of {@code -1} indicates that the cell is empty (destroyed
 * by a match sequence and awaiting refill by gravity).</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class Box {

    /**
     * Row index of this cell in the grid (vertical axis).
     */
    private int x;

    /**
     * Column index of this cell in the grid (horizontal axis).
     */
    private int y;

    /**
     * Color identifier of this cell (0 to {@code Grid.COLOR_COUNT - 1}),
     * or {@code -1} if the cell is empty.
     */
    private int color;

    /**
     * Whether this cell is available for movement.
     * A locked cell blocks sliding of its entire row and column.
     */
    private boolean available;

    /**
     * Constructs a cell with the given attributes.
     *
     * @param x         the row index of this cell in the grid
     * @param y         the column index of this cell in the grid
     * @param color     the color identifier (0 to {@code COLOR_COUNT - 1}, or {@code -1} if empty)
     * @param available {@code true} if the cell is available for movement
     */
    public Box(int x, int y, int color, boolean available) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.available = available;
    }

    /**
     * Returns the row index of this cell in the grid.
     *
     * @return the row index
     */
    public int getX() {
        return this.x;
    }

    /**
     * Sets the row index of this cell in the grid.
     *
     * @param x the new row index
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Returns the column index of this cell in the grid.
     *
     * @return the column index
     */
    public int getY() {
        return this.y;
    }

    /**
     * Sets the column index of this cell in the grid.
     *
     * @param y the new column index
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Returns the color identifier of this cell.
     *
     * @return the color identifier (0 to {@code COLOR_COUNT - 1}), or {@code -1} if the cell is empty
     */
    public int getColor() {
        return this.color;
    }

    /**
     * Sets the color identifier of this cell.
     *
     * @param color the new color identifier, or {@code -1} to mark the cell as empty
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Returns whether this cell is available for movement.
     *
     * @return {@code true} if the cell is available, {@code false} if it is locked
     */
    public boolean isAvailable() {
        return this.available;
    }

    /**
     * Sets the availability of this cell.
     *
     * @param available {@code true} to unlock the cell, {@code false} to lock it
     */
    public void setAvailability(boolean available) {
        this.available = available;
    }
}
