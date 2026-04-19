package fr.iut_fbleau.chuzzle.controller.game;

import android.view.MotionEvent;
import android.view.View;

import fr.iut_fbleau.chuzzle.model.grid.Grid;
import fr.iut_fbleau.chuzzle.view.GameView;

/**
 * The <code>GameTouchController</code> class handles touch input on the game board.
 * It implements {@link View.OnTouchListener} and translates raw touch events into
 * row or column slide gestures that are forwarded to the {@link GameLoop}.
 *
 * <p>Once the finger has moved beyond the threshold, the dominant axis (horizontal
 * or vertical) is locked for the rest of the gesture. Historical batched events
 * from {@link MotionEvent#ACTION_MOVE} are processed before the current event to
 * ensure smooth drag feedback.</p>
 *
 * <p>If the targeted row or column is locked, an error highlight is shown on the
 * view instead of a drag animation.</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class GameTouchController implements View.OnTouchListener {

    /**
     * Axis constant indicating that no drag direction has been determined yet.
     */
    private static final int AXIS_NONE = 0;

    /**
     * Axis constant indicating a horizontal (row) drag.
     */
    private static final int AXIS_HORIZONTAL = 1;

    /**
     * Axis constant indicating a vertical (column) drag.
     */
    private static final int AXIS_VERTICAL = 2;

    /**
     * The game view used for drag offset and error highlight feedback.
     */
    private GameView view;

    /**
     * The game loop that executes the validated turn.
     */
    private GameLoop gameLoop;

    /**
     * X coordinate (in pixels) of the initial touch-down point.
     */
    private float startX;

    /**
     * Y coordinate (in pixels) of the initial touch-down point.
     */
    private float startY;

    /**
     * Minimum displacement in pixels required before a drag is recognized.
     */
    private float threshold = 30f;

    /**
     * Whether a drag gesture is currently in progress.
     */
    private boolean isDragging = false;

    /**
     * The axis along which the current drag is locked ({@link #AXIS_NONE},
     * {@link #AXIS_HORIZONTAL}, or {@link #AXIS_VERTICAL}).
     */
    private int lockedAxis = AXIS_NONE;

    /**
     * The row being dragged, or {@code -1} if the drag is vertical.
     */
    private int lockedRow = -1;

    /**
     * The column being dragged, or {@code -1} if the drag is horizontal.
     */
    private int lockedCol = -1;

    /**
     * Constructs a touch controller bound to the given view and game loop.
     *
     * @param view     the game view that provides drag and error feedback
     * @param gameLoop the game loop that validates and executes moves
     */
    public GameTouchController(GameView view, GameLoop gameLoop) {
        this.view = view;
        this.gameLoop = gameLoop;
    }

    /**
     * Receives touch events from the game view and dispatches them to the
     * appropriate handler. Input is ignored while the game loop is resolving a turn.
     *
     * @param v     the view that received the touch event
     * @param event the motion event describing the touch
     * @return {@code true} to indicate the event has been consumed
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (this.gameLoop.isResolvingTurn()) {
            return false;
        }

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                this.startX = event.getX();
                this.startY = event.getY();
                this.resetGestureState();
                break;

            case MotionEvent.ACTION_MOVE:
                int historySize = event.getHistorySize();
                for (int h = 0; h < historySize; h++) {
                    this.handleMove(event.getHistoricalX(h), event.getHistoricalY(h));
                }
                this.handleMove(event.getX(), event.getY());
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                this.handleUp(v, event.getX(), event.getY());
                break;
        }
        return true;
    }

    /**
     * Processes an intermediate touch position during a drag.
     * Locks the drag axis once the displacement exceeds the threshold, then
     * updates the view's drag offset or shows the error highlight.
     *
     * @param x the current X coordinate
     * @param y the current Y coordinate
     */
    private void handleMove(float x, float y) {
        float deltaX = x - this.startX;
        float deltaY = y - this.startY;

        if (!this.isDragging && (Math.abs(deltaX) > this.threshold || Math.abs(deltaY) > this.threshold)) {
            this.startDragging(deltaX, deltaY);
        }

        if (this.isDragging) {
            if (this.lockedAxis == AXIS_HORIZONTAL) {
                if (this.gameLoop.canMoveRow(this.lockedRow)) {
                    this.view.setDragOffset(this.lockedRow, -1, deltaX);
                } else {
                    this.view.setErrorHighlight(this.lockedRow, -1);
                }
            } else if (this.lockedAxis == AXIS_VERTICAL) {
                if (this.gameLoop.canMoveColumn(this.lockedCol)) {
                    this.view.setDragOffset(-1, this.lockedCol, deltaY);
                } else {
                    this.view.setErrorHighlight(-1, this.lockedCol);
                }
            } else {
                this.view.resetDragOffset();
            }
        }
    }

    /**
     * Initiates a drag gesture by determining the locked axis and the targeted
     * row or column from the initial touch-down position.
     * Does nothing if the touch-down point falls outside the board.
     *
     * @param deltaX the horizontal displacement used to determine the dominant axis
     * @param deltaY the vertical displacement used to determine the dominant axis
     */
    private void startDragging(float deltaX, float deltaY) {
        int row = this.getRowFromY(this.startY);
        int col = this.getColFromX(this.startX);
        if (row == -1 || col == -1) {
            return;
        }

        this.isDragging = true;
        this.lockedRow = row;
        this.lockedCol = col;
        this.lockedAxis = Math.abs(deltaX) > Math.abs(deltaY) ? AXIS_HORIZONTAL : AXIS_VERTICAL;
    }

    /**
     * Resets all gesture state variables to their default values.
     * Called at the start of each new touch-down event.
     */
    private void resetGestureState() {
        this.isDragging = false;
        this.lockedAxis = AXIS_NONE;
        this.lockedRow = -1;
        this.lockedCol = -1;
    }

    /**
     * Handles the finger-up event at the end of a gesture.
     * If a drag was in progress, computes the shift distance in cells and
     * submits the turn to the game loop. Otherwise, delegates to
     * {@link View#performClick()}.
     *
     * @param v the view that received the event
     * @param x the X coordinate of the finger-up position
     * @param y the Y coordinate of the finger-up position
     */
    private void handleUp(View v, float x, float y) {
        if (this.isDragging) {
            float deltaX = x - this.startX;
            float deltaY = y - this.startY;
            float boardSize = Math.min(this.view.getWidth(), this.view.getHeight());
            float cellSize = boardSize / Grid.GRID_SIZE;

            if (this.lockedAxis == AXIS_HORIZONTAL) {
                if (this.gameLoop.canMoveRow(this.lockedRow)) {
                    int shift = Math.round(Math.abs(deltaX) / cellSize);
                    if (deltaX < 0) {
                        shift = Grid.GRID_SIZE - (shift % Grid.GRID_SIZE);
                    }
                    this.gameLoop.executeTurn(true, this.lockedRow, shift);
                }
            } else if (this.lockedAxis == AXIS_VERTICAL) {
                if (this.gameLoop.canMoveColumn(this.lockedCol)) {
                    int shift = Math.round(Math.abs(deltaY) / cellSize);
                    if (deltaY < 0) {
                        shift = Grid.GRID_SIZE - (shift % Grid.GRID_SIZE);
                    }
                    this.gameLoop.executeTurn(false, this.lockedCol, shift);
                }
            } else {
                v.performClick();
            }
        } else {
            v.performClick();
        }
        this.view.resetDragOffset();
        this.resetGestureState();
    }

    /**
     * Converts a Y screen coordinate into the corresponding grid row index.
     *
     * @param y the Y coordinate in pixels relative to the view
     * @return the row index (0 to {@code GRID_SIZE - 1}), or {@code -1} if outside the board
     */
    private int getRowFromY(float y) {
        float boardSize = Math.min(this.view.getWidth(), this.view.getHeight());
        float offsetY = (this.view.getHeight() - boardSize) / 2f;
        float cellSize = boardSize / Grid.GRID_SIZE;
        int row = (int) ((y - offsetY) / cellSize);
        return (row < 0 || row >= Grid.GRID_SIZE) ? -1 : row;
    }

    /**
     * Converts an X screen coordinate into the corresponding grid column index.
     *
     * @param x the X coordinate in pixels relative to the view
     * @return the column index (0 to {@code GRID_SIZE - 1}), or {@code -1} if outside the board
     */
    private int getColFromX(float x) {
        float boardSize = Math.min(this.view.getWidth(), this.view.getHeight());
        float offsetX = (this.view.getWidth() - boardSize) / 2f;
        float cellSize = boardSize / Grid.GRID_SIZE;
        int col = (int) ((x - offsetX) / cellSize);
        return (col < 0 || col >= Grid.GRID_SIZE) ? -1 : col;
    }
}
