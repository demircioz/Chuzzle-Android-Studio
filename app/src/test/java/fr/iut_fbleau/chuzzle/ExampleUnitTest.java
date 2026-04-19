package fr.iut_fbleau.chuzzle;

import org.junit.Test;

import java.lang.reflect.Field;

import fr.iut_fbleau.chuzzle.model.box.Box;
import fr.iut_fbleau.chuzzle.model.grid.Grid;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void generatedGrid_startsPlayableWithoutMatches() {
        Grid grid = new Grid(123456789L);

        assertTrue(grid.hasPossibleMoves());
        assertFalse(hasAnyMatch(grid.getTable()));
    }

    @Test
    public void hasPossibleMoves_detectsPlayableBoard() throws Exception {
        Grid grid = createGrid(new int[][]{
                {4, 1, 6, 2, 1, 0},
                {1, 4, 4, 2, 4, 1},
                {3, 6, 3, 3, 1, 0},
                {4, 6, 6, 1, 1, 2},
                {1, 4, 1, 5, 5, 2},
                {4, 5, 2, 0, 6, 1}
        });

        assertTrue(grid.hasPossibleMoves());
    }

    @Test
    public void hasPossibleMoves_detectsBlockedBoard() throws Exception {
        Grid grid = createGrid(new int[][]{
                {3, 5, 3, 0, 1, 3},
                {2, 5, 6, 4, 5, 6},
                {1, 4, 3, 4, 0, 1},
                {0, 5, 5, 0, 1, 5},
                {2, 4, 1, 6, 3, 0},
                {4, 2, 6, 6, 0, 6}
        });

        assertFalse(grid.hasPossibleMoves());
    }

    private Grid createGrid(int[][] colors) throws Exception {
        Grid grid = new Grid(1L);
        Box[][] table = new Box[colors.length][colors[0].length];

        for (int row = 0; row < colors.length; row++) {
            for (int col = 0; col < colors[row].length; col++) {
                table[row][col] = new Box(row, col, colors[row][col], true);
            }
        }

        Field tableField = Grid.class.getDeclaredField("table");
        tableField.setAccessible(true);
        tableField.set(grid, table);

        return grid;
    }

    private boolean hasAnyMatch(Box[][] table) {
        int size = table.length;

        for (int row = 0; row < size; row++) {
            for (int col = 0; col <= size - 3; col++) {
                int color = table[row][col].getColor();
                if (color == table[row][col + 1].getColor()
                        && color == table[row][col + 2].getColor()) {
                    return true;
                }
            }
        }

        for (int col = 0; col < size; col++) {
            for (int row = 0; row <= size - 3; row++) {
                int color = table[row][col].getColor();
                if (color == table[row + 1][col].getColor()
                        && color == table[row + 2][col].getColor()) {
                    return true;
                }
            }
        }

        return false;
    }
}
