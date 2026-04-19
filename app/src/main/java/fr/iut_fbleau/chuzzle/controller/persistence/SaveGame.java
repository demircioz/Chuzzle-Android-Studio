package fr.iut_fbleau.chuzzle.controller.persistence;

import java.util.Random;

/**
 * The <code>SaveGame</code> class is a plain data object that holds a complete
 * snapshot of a game session. It is used to persist and restore the game state
 * across activity lifecycle events (pause, rotation, process death).
 *
 * <p>Instances are produced by {@link SaveGameManager} and consumed by
 * {@link fr.iut_fbleau.chuzzle.model.grid.Grid#restoreState} to resume a saved
 * session exactly where it was left.</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class SaveGame {

    /**
     * Seed used to initialise the grid's pseudo-random generator,
     * allowing the session to be reproduced.
     */
    private long seed;

    /**
     * The player's score at the time the snapshot was taken.
     */
    private int score;

    /**
     * The number of moves played at the time the snapshot was taken.
     */
    private int moves;

    /**
     * Flat array of color identifiers for every cell (row-major order).
     * A value of {@code -1} indicates an empty cell.
     */
    private int[] colors;

    /**
     * Flat array of availability flags for every cell (row-major order).
     * {@code false} means the cell is locked.
     */
    private boolean[] avails;

    /**
     * The pseudo-random generator state at the time of the snapshot,
     * serialised so that future random draws reproduce exactly.
     */
    private Random generator;

    /**
     * Whether gravity was inverted at the time the snapshot was taken.
     */
    private boolean invertedGravity;

    /**
     * Constructs an empty save game. Fields must be set individually before use.
     */
    public SaveGame() {
    }

    /**
     * Constructs a fully populated save game snapshot.
     *
     * @param seed            the grid seed
     * @param score           the current score
     * @param moves           the number of moves played
     * @param colors          flat array of cell color identifiers (row-major)
     * @param avails          flat array of cell availability flags (row-major)
     * @param generator       the current pseudo-random generator state
     * @param invertedGravity whether gravity is currently inverted
     */
    public SaveGame(long seed, int score, int moves, int[] colors, boolean[] avails, Random generator, boolean invertedGravity) {
        this.seed = seed;
        this.score = score;
        this.moves = moves;
        this.colors = colors;
        this.avails = avails;
        this.generator = generator;
        this.invertedGravity = invertedGravity;
    }

    /**
     * Returns the grid seed.
     *
     * @return the seed value
     */
    public long getSeed() {
        return this.seed;
    }

    /**
     * Sets the grid seed.
     *
     * @param seed the seed value
     */
    public void setSeed(long seed) {
        this.seed = seed;
    }

    /**
     * Returns the player's score.
     *
     * @return the score
     */
    public int getScore() {
        return this.score;
    }

    /**
     * Sets the player's score.
     *
     * @param score the score
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Returns the number of moves played.
     *
     * @return the move count
     */
    public int getMoves() {
        return this.moves;
    }

    /**
     * Sets the number of moves played.
     *
     * @param moves the move count
     */
    public void setMoves(int moves) {
        this.moves = moves;
    }

    /**
     * Returns the flat array of cell color identifiers (row-major order).
     *
     * @return the color array
     */
    public int[] getColors() {
        return this.colors;
    }

    /**
     * Sets the flat array of cell color identifiers.
     *
     * @param colors the color array (row-major order)
     */
    public void setColors(int[] colors) {
        this.colors = colors;
    }

    /**
     * Returns the flat array of cell availability flags (row-major order).
     *
     * @return the availability array
     */
    public boolean[] getAvails() {
        return this.avails;
    }

    /**
     * Sets the flat array of cell availability flags.
     *
     * @param avails the availability array (row-major order)
     */
    public void setAvails(boolean[] avails) {
        this.avails = avails;
    }

    /**
     * Returns the pseudo-random generator state.
     *
     * @return the generator
     */
    public Random getGenerator() {
        return this.generator;
    }

    /**
     * Sets the pseudo-random generator state.
     *
     * @param generator the generator
     */
    public void setGenerator(Random generator) {
        this.generator = generator;
    }

    /**
     * Returns whether gravity was inverted when this snapshot was taken.
     *
     * @return {@code true} if gravity is inverted
     */
    public boolean isInvertedGravity() {
        return this.invertedGravity;
    }

    /**
     * Sets whether gravity is inverted.
     *
     * @param invertedGravity {@code true} if gravity is inverted
     */
    public void setInvertedGravity(boolean invertedGravity) {
        this.invertedGravity = invertedGravity;
    }
}
