package fr.iut_fbleau.chuzzle.controller.game;

import fr.iut_fbleau.chuzzle.model.grid.Grid;
import fr.iut_fbleau.chuzzle.view.GameView;
import fr.iut_fbleau.chuzzle.controller.activities.GameActivity;

/**
 * The <code>GameLoop</code> class coordinates the execution of a single game turn.
 * It validates player moves, computes scores, manages lock events, and checks
 * whether the game is over.
 *
 * <p>A turn is initiated by {@link #executeTurn}. If the move creates no match,
 * it is silently reversed. Otherwise, cascading matches are resolved, the score
 * is updated, a lock may be applied, and the game-over condition is evaluated.</p>
 *
 * <p>The hard mode flag increases lock frequency and activates an additional
 * score multiplier.</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class GameLoop {

    /**
     * The grid model on which moves are applied.
     */
    private Grid grid;

    /**
     * The view that must be invalidated after every state change.
     */
    private GameView view;

    /**
     * The activity used to update the score/moves HUD and trigger game-over.
     */
    private GameActivity activity;

    /**
     * Whether a turn is currently being resolved.
     * Touch input is blocked while this flag is {@code true}.
     */
    private boolean resolvingTurn = false;

    /**
     * Whether hard mode is enabled.
     * Hard mode increases lock probability and applies a score multiplier.
     */
    private boolean hardMode = false;

    /**
     * Constructs a game loop bound to the given model, view, and activity.
     *
     * @param grid     the grid model to operate on
     * @param view     the game view to refresh after each state change
     * @param activity the game activity used for HUD updates and game-over notification
     */
    public GameLoop(Grid grid, GameView view, GameActivity activity) {
        this.grid = grid;
        this.view = view;
        this.activity = activity;
    }

    /**
     * Enables or disables hard mode.
     *
     * @param hardMode {@code true} to activate hard mode
     */
    public void setHardMode(boolean hardMode) {
        this.hardMode = hardMode;
    }

    /**
     * Returns whether a turn is currently being resolved.
     * Touch events should be ignored while this returns {@code true}.
     *
     * @return {@code true} if a turn resolution is in progress
     */
    public boolean isResolvingTurn() {
        return this.resolvingTurn;
    }

    /**
     * Returns whether every cell in the given row is available for movement.
     *
     * @param rowIndex the row index to check
     * @return {@code true} if the row can be shifted
     */
    public boolean canMoveRow(int rowIndex) {
        return this.grid.canMoveRow(rowIndex);
    }

    /**
     * Returns whether every cell in the given column is available for movement.
     *
     * @param colIndex the column index to check
     * @return {@code true} if the column can be shifted
     */
    public boolean canMoveColumn(int colIndex) {
        return this.grid.canMoveColumn(colIndex);
    }

    /**
     * Executes a full turn: applies the requested move, resolves all cascading
     * matches, updates the score, and checks the game-over condition.
     * If the move creates no match, it is reversed and nothing is recorded.
     * Does nothing if a turn is already being resolved.
     *
     * @param isHorizontal {@code true} to shift a row, {@code false} to shift a column
     * @param index        the index of the row or column to shift
     * @param distance     the number of positions to shift (positive = right/down)
     */
    public void executeTurn(boolean isHorizontal, int index, int distance) {
        if (this.resolvingTurn) {
            return;
        }

        this.resolvingTurn = true;
        try {
            this.move(isHorizontal, index, distance);

            int[] resolution = this.grid.resolveTurnDetails();

            if (resolution[1] == 0) {
                this.move(isHorizontal, index, -distance);
                this.view.invalidate();
            } else {
                this.processValidMove(resolution);
            }
        } finally {
            this.resolvingTurn = false;
        }
    }

    /**
     * Handles the aftermath of a valid move: increments the move counter,
     * optionally toggles gravity in hard mode, manages the lock chance,
     * adds the earned score, refreshes the view, and checks for game over.
     *
     * @param resolution the resolution array {@code [totalPoints, totalSeries]}
     *                   returned by {@link Grid#resolveTurnDetails()}
     */
    private void processValidMove(int[] resolution) {
        this.activity.incrementMoves();

        if (this.hardMode && this.activity.getMovesCount() % 5 == 0) {
            this.grid.setInvertedGravity(!this.grid.isInvertedGravity());
        }

        this.manageLockChance();
        this.activity.addScore(this.calculateFinalScore(resolution[0], resolution[1]));
        this.view.invalidate();

        this.checkGameState();
    }

    /**
     * Checks whether any valid move remains. If not, notifies the activity
     * that the game is over. Otherwise, persists the current game state.
     */
    private void checkGameState() {
        if (!this.grid.hasPossibleMoves()) {
            this.activity.gameOver();
        } else {
            this.activity.persistCurrentGame();
        }
    }

    /**
     * Applies the given directional shift to the grid model if the targeted
     * row or column is not locked.
     *
     * @param isHorizontal {@code true} to shift a row, {@code false} to shift a column
     * @param index        the index of the row or column to shift
     * @param distance     the number of positions (positive = right/down, negative = left/up)
     */
    private void move(boolean isHorizontal, int index, int distance) {
        if (isHorizontal) {
            if (this.grid.canMoveRow(index)) {
                if (distance > 0) {
                    this.grid.moveRight(index, Math.abs(distance));
                } else {
                    this.grid.moveLeft(index, Math.abs(distance));
                }
            }
        } else {
            if (this.grid.canMoveColumn(index)) {
                if (distance > 0) {
                    this.grid.moveDown(index, Math.abs(distance));
                } else {
                    this.grid.moveUp(index, Math.abs(distance));
                }
            }
        }
    }

    /**
     * Evaluates the probability of a lock event occurring at the end of this move
     * and locks a random cell if the event triggers.
     * The probability grows with the number of moves played, capped at a maximum.
     */
    private void manageLockChance() {
        int moves = this.activity.getMovesCount();
        double baseChance = this.hardMode ? 0.10 : 0.05;
        double growthFactor = this.hardMode ? 0.10 : 0.05;
        double cap = this.hardMode ? 0.50 : 0.33;

        double chance = baseChance + (moves / 10.0) * growthFactor;
        if (chance > cap) {
            chance = cap;
        }

        if (this.grid.shouldLockBox(chance)) {
            this.grid.lockRandomBox();
        }
    }

    /**
     * Computes the final score for a turn given the total base points and
     * number of series. Applies a +50% bonus for each series beyond the first,
     * and an additional 1.5× multiplier in hard mode.
     *
     * @param totalBasePoints the sum of base point values across all series and cascades
     * @param totalSeries     the total number of series cleared during the turn
     * @return the final score to add to the player's total
     */
    private int calculateFinalScore(int totalBasePoints, int totalSeries) {
        double multiplier = 1.0 + ((totalSeries - 1) * 0.5);
        if (this.hardMode) {
            multiplier *= 1.5;
        }
        return (int) (totalBasePoints * multiplier);
    }
}
