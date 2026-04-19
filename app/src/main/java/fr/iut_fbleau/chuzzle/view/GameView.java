package fr.iut_fbleau.chuzzle.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import fr.iut_fbleau.chuzzle.R;
import fr.iut_fbleau.chuzzle.model.box.Box;
import fr.iut_fbleau.chuzzle.model.grid.Grid;

/**
 * The <code>GameView</code> class is the custom {@link View} responsible for
 * rendering the Chuzzle game board on screen.
 *
 * <p>It draws each cell's bitmap, the grid background, the lock overlay for
 * unavailable cells, and animated drag offsets while the player is sliding a
 * row or column. An error highlight is shown in red when the player attempts
 * to move a locked row or column.</p>
 *
 * <p>Hardware acceleration is enabled; shadow layers must not be used.</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class GameView extends View {

    /**
     * The grid model whose state is rendered by this view.
     */
    private Grid grid;

    /**
     * Bitmaps for each color type, indexed by color identifier.
     */
    private Bitmap[] boxImages;

    /**
     * Paint used to draw empty (destroyed) cells.
     */
    private Paint emptyPaint;

    /**
     * Paint used to highlight a locked row or column in red when the player
     * attempts an invalid move.
     */
    private Paint highlightPaint;

    /**
     * Paint used to draw the rounded background of the entire board.
     */
    private Paint gridBgPaint;

    /**
     * Paint used to draw the individual cell backgrounds.
     */
    private Paint cellBgPaint;

    /**
     * Paint used to draw the border outline of the board.
     */
    private Paint gridOutlinePaint;

    /**
     * Bitmap drawn on top of locked cells to indicate they cannot be moved.
     */
    private Bitmap lockImage;

    /**
     * Computed size (in pixels) of a single cell, updated every draw pass.
     */
    private float cellSize;

    /**
     * Margin (in pixels) between a cell's background and its drawn bitmap.
     */
    private static final float MARGIN = 8f;

    /**
     * Margin (in pixels) between adjacent cell backgrounds.
     */
    private static final float CELL_BG_MARGIN = 4f;

    /**
     * Index of the row currently being dragged, or {@code -1} if none.
     */
    private int movingRow = -1;

    /**
     * Index of the column currently being dragged, or {@code -1} if none.
     */
    private int movingCol = -1;

    /**
     * Current drag offset in pixels applied to the moving row or column.
     */
    private float dragOffsetPixel = 0f;

    /**
     * Index of the row shown with an error highlight, or {@code -1} if none.
     */
    private int errorRow = -1;

    /**
     * Index of the column shown with an error highlight, or {@code -1} if none.
     */
    private int errorCol = -1;

    /** Reusable rectangle representing the full board area. */
    private final RectF boardRect = new RectF();

    /** Reusable rectangle representing a single cell background. */
    private final RectF cellRect = new RectF();

    /** Reusable rectangle representing a single cell's bitmap area. */
    private final RectF boxRect = new RectF();

    /** Reusable rectangle representing the destination area of the lock icon. */
    private final RectF lockDestRect = new RectF();

    /**
     * Constructs the view from XML layout inflation.
     *
     * @param context the Android context
     * @param attrs   the attribute set from the XML layout
     */
    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initPaints();
    }

    /**
     * Initialises all paints, loads all color bitmaps and the lock icon
     * from drawable resources.
     */
    private void initPaints() {
        this.boxImages = new Bitmap[Grid.COLOR_COUNT];
        Resources res = this.getResources();

        this.boxImages[0] = BitmapFactory.decodeResource(res, R.drawable.blue);
        this.boxImages[1] = BitmapFactory.decodeResource(res, R.drawable.yellow);
        this.boxImages[2] = BitmapFactory.decodeResource(res, R.drawable.dark_red);
        this.boxImages[3] = BitmapFactory.decodeResource(res, R.drawable.purple);
        this.boxImages[4] = BitmapFactory.decodeResource(res, R.drawable.red);
        this.boxImages[5] = BitmapFactory.decodeResource(res, R.drawable.orange);
        this.boxImages[6] = BitmapFactory.decodeResource(res, R.drawable.green);

        this.emptyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.emptyPaint.setColor(Color.TRANSPARENT);

        this.highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.highlightPaint.setColor(Color.argb(120, 255, 0, 0));
        this.highlightPaint.setStyle(Paint.Style.FILL);

        this.gridBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.gridBgPaint.setColor(Color.argb(180, 200, 200, 200));
        this.gridBgPaint.setStyle(Paint.Style.FILL);

        this.gridOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.gridOutlinePaint.setColor(Color.argb(200, 50, 50, 50));
        this.gridOutlinePaint.setStyle(Paint.Style.STROKE);
        this.gridOutlinePaint.setStrokeWidth(6f);

        this.cellBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.cellBgPaint.setColor(Color.argb(100, 255, 255, 255));
        this.cellBgPaint.setStyle(Paint.Style.FILL);

        this.lockImage = BitmapFactory.decodeResource(res, R.drawable.lock);
    }

    /**
     * Sets the grid model to render and triggers a redraw.
     *
     * @param grid the grid to display
     */
    public void setGrid(Grid grid) {
        this.grid = grid;
        this.invalidate();
    }

    /**
     * Sets the current drag state for a row or column and triggers a redraw.
     * Clears any active error highlight.
     *
     * @param row         the row being dragged, or {@code -1} if dragging a column
     * @param col         the column being dragged, or {@code -1} if dragging a row
     * @param offsetPixel the drag offset in pixels
     */
    public void setDragOffset(int row, int col, float offsetPixel) {
        this.movingRow = row;
        this.movingCol = col;
        this.dragOffsetPixel = offsetPixel;
        this.errorRow = -1;
        this.errorCol = -1;
        this.invalidate();
    }

    /**
     * Sets an error highlight on a locked row or column and triggers a redraw.
     * Clears any active drag offset.
     *
     * @param row the row to highlight in red, or {@code -1} if highlighting a column
     * @param col the column to highlight in red, or {@code -1} if highlighting a row
     */
    public void setErrorHighlight(int row, int col) {
        this.errorRow = row;
        this.errorCol = col;
        this.movingRow = -1;
        this.movingCol = -1;
        this.dragOffsetPixel = 0f;
        this.invalidate();
    }

    /**
     * Resets all drag and error state, then triggers a redraw.
     */
    public void resetDragOffset() {
        this.movingRow = -1;
        this.movingCol = -1;
        this.dragOffsetPixel = 0f;
        this.errorRow = -1;
        this.errorCol = -1;
        this.invalidate();
    }

    /**
     * Called by the system when this view needs to be drawn.
     * Renders the board only when the grid model is available.
     *
     * @param canvas the canvas on which to draw
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (this.grid != null && this.grid.getTable() != null) {
            this.drawBoard(canvas);
        }
    }

    /**
     * Delegates the click event to the superclass to satisfy accessibility requirements.
     *
     * @return the result of {@link View#performClick()}
     */
    @Override
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * Draws the full board: background, cell backgrounds, all cell bitmaps
     * (with drag offset applied to the moving row or column), and the error
     * highlight if applicable.
     *
     * @param canvas the canvas on which to draw
     */
    private void drawBoard(Canvas canvas) {
        float boardSize = Math.min(this.getWidth(), this.getHeight());
        this.cellSize = boardSize / Grid.GRID_SIZE;
        float offsetX = (this.getWidth() - boardSize) / 2f;
        float offsetY = (this.getHeight() - boardSize) / 2f;

        canvas.save();
        canvas.translate(offsetX, offsetY);

        this.boardRect.set(0, 0, boardSize, boardSize);
        canvas.drawRoundRect(this.boardRect, 25f, 25f, this.gridBgPaint);
        canvas.drawRoundRect(this.boardRect, 25f, 25f, this.gridOutlinePaint);

        canvas.clipRect(0, 0, boardSize, boardSize);

        for (int i = 0; i < Grid.GRID_SIZE; i++) {
            for (int j = 0; j < Grid.GRID_SIZE; j++) {
                this.cellRect.set((j * this.cellSize) + CELL_BG_MARGIN,
                        (i * this.cellSize) + CELL_BG_MARGIN,
                        ((j + 1) * this.cellSize) - CELL_BG_MARGIN,
                        ((i + 1) * this.cellSize) - CELL_BG_MARGIN);
                canvas.drawRoundRect(this.cellRect, 20f, 20f, this.cellBgPaint);
            }
        }

        float effectiveOffset = this.dragOffsetPixel % boardSize;
        float circularOffset = (effectiveOffset > 0) ? effectiveOffset - boardSize : effectiveOffset + boardSize;

        for (int i = 0; i < Grid.GRID_SIZE; i++) {
            for (int j = 0; j < Grid.GRID_SIZE; j++) {
                if (i == this.movingRow) {
                    this.drawSingleBoxLogicalAt(canvas, i, j, effectiveOffset, 0);
                    this.drawSingleBoxLogicalAt(canvas, i, j, circularOffset, 0);
                } else if (j == this.movingCol) {
                    this.drawSingleBoxLogicalAt(canvas, i, j, 0, effectiveOffset);
                    this.drawSingleBoxLogicalAt(canvas, i, j, 0, circularOffset);
                } else {
                    this.drawSingleBoxLogicalAt(canvas, i, j, 0, 0);
                }
            }
        }

        if (this.errorRow != -1) {
            canvas.drawRect(0, this.errorRow * this.cellSize, boardSize, (this.errorRow + 1) * this.cellSize, this.highlightPaint);
        } else if (this.errorCol != -1) {
            canvas.drawRect(this.errorCol * this.cellSize, 0, (this.errorCol + 1) * this.cellSize, boardSize, this.highlightPaint);
        }

        canvas.restore();
    }

    /**
     * Draws the bitmap of the cell at logical position {@code (i, j)} with the
     * given pixel offsets applied. Also draws the lock icon if the cell is locked.
     *
     * @param canvas the canvas on which to draw
     * @param i      the logical row index of the cell
     * @param j      the logical column index of the cell
     * @param dx     the horizontal pixel offset to apply (drag translation)
     * @param dy     the vertical pixel offset to apply (drag translation)
     */
    private void drawSingleBoxLogicalAt(Canvas canvas, int i, int j, float dx, float dy) {
        Box box = this.grid.getTable()[i][j];
        int colorIndex = box.getColor();

        float left = (j * this.cellSize) + MARGIN;
        float top = (i * this.cellSize) + MARGIN;
        float right = ((j + 1) * this.cellSize) - MARGIN;
        float bottom = ((i + 1) * this.cellSize) - MARGIN;

        this.boxRect.set(left + dx, top + dy, right + dx, bottom + dy);

        if (colorIndex >= 0 && colorIndex < Grid.COLOR_COUNT) {
            Bitmap bitmap = this.boxImages[colorIndex];
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, null, this.boxRect, null);
            }
            this.drawLockIfNeeded(canvas, box, this.boxRect);
        } else if (colorIndex == -1) {
            canvas.drawRoundRect(this.boxRect, 20f, 20f, this.emptyPaint);
        }
    }

    /**
     * Draws the lock icon over the given rectangle if the cell is locked.
     *
     * @param canvas the canvas on which to draw
     * @param box    the cell whose lock state is checked
     * @param rect   the destination rectangle where the cell bitmap was drawn
     */
    private void drawLockIfNeeded(Canvas canvas, Box box, RectF rect) {
        if (!box.isAvailable() && this.lockImage != null) {
            float padding = this.cellSize * 0.10f;
            this.lockDestRect.set(rect.left + padding,
                    rect.top + padding,
                    rect.right - padding,
                    rect.bottom - padding);
            canvas.drawBitmap(this.lockImage, null, this.lockDestRect, null);
        }
    }
}
