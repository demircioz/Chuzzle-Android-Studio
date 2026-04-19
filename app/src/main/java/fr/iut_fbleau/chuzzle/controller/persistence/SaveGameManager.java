package fr.iut_fbleau.chuzzle.controller.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import fr.iut_fbleau.chuzzle.model.box.Box;
import fr.iut_fbleau.chuzzle.model.grid.Grid;

/**
 * The <code>SaveGameManager</code> class handles all persistence operations for
 * the game state. It can save and load a {@link SaveGame} snapshot to and from
 * {@link SharedPreferences}, as well as write and read the state to and from
 * an Android {@link Bundle} (used for activity state restoration on rotation).
 *
 * <p>The {@link Random} generator is serialised to a Base64-encoded string via
 * Java object serialisation so that the exact pseudo-random sequence can be
 * resumed on restore.</p>
 *
 * <p>This class is purely static and cannot be instantiated meaningfully.</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public final class SaveGameManager {

    /**
     * Expected number of cells in a saved grid ({@code GRID_SIZE × GRID_SIZE}).
     */
    private static final int GRID_CELL_COUNT = Grid.GRID_SIZE * Grid.GRID_SIZE;

    /** Name of the {@link SharedPreferences} file used for game persistence. */
    private static final String PREFS_NAME = "chuzzle_saved_game";

    /** Key for the boolean flag indicating that a save exists. */
    private static final String KEY_HAS_SAVE = "has_save";

    /** Key for the grid seed value. */
    private static final String KEY_SEED = "seed";

    /** Key for the player's score. */
    private static final String KEY_SCORE = "score";

    /** Key for the move count. */
    private static final String KEY_MOVES = "moves";

    /** Key for the serialised cell color array. */
    private static final String KEY_COLORS = "colors";

    /** Key for the serialised cell availability array. */
    private static final String KEY_AVAILS = "avails";

    /** Key for the Base64-encoded serialised {@link Random} generator. */
    private static final String KEY_RANDOM = "random";

    /** Key for the inverted gravity flag. */
    private static final String KEY_INVERTED_GRAVITY = "inverted_gravity";

    /**
     * Default constructor. This class is not meant to be instantiated;
     * all methods are static.
     */
    public SaveGameManager() {
    }

    /**
     * Returns whether a saved game is currently stored in {@link SharedPreferences}.
     *
     * @param context the Android context used to access preferences
     * @return {@code true} if a save exists
     */
    public static boolean hasSavedGame(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(KEY_HAS_SAVE, false);
    }

    /**
     * Captures the current game state and writes it to {@link SharedPreferences}.
     * Does nothing if the grid or its generator is {@code null}, or if the
     * generator cannot be serialised.
     *
     * @param context the Android context used to access preferences
     * @param grid    the grid model to save
     * @param score   the current player score
     * @param moves   the current move count
     */
    public static void save(Context context, Grid grid, int score, int moves) {
        SaveGame saveGame = capture(grid, score, moves);
        if (saveGame == null) {
            return;
        }

        String serializedRandom = serializeRandom(saveGame.getGenerator());
        if (serializedRandom == null) {
            return;
        }

        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putBoolean(KEY_HAS_SAVE, true)
                .putLong(KEY_SEED, saveGame.getSeed())
                .putInt(KEY_SCORE, saveGame.getScore())
                .putInt(KEY_MOVES, saveGame.getMoves())
                .putString(KEY_COLORS, serializeIntArray(saveGame.getColors()))
                .putString(KEY_AVAILS, serializeBooleanArray(saveGame.getAvails()))
                .putString(KEY_RANDOM, serializedRandom)
                .putBoolean(KEY_INVERTED_GRAVITY, saveGame.isInvertedGravity())
                .apply();
    }

    /**
     * Loads the saved game from {@link SharedPreferences} and returns it.
     * If no valid save is found, clears the corrupted data and returns {@code null}.
     *
     * @param context the Android context used to access preferences
     * @return the loaded {@link SaveGame}, or {@code null} if none exists or the data is invalid
     */
    public static SaveGame load(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (!preferences.getBoolean(KEY_HAS_SAVE, false)) {
            return null;
        }

        long seed = preferences.getLong(KEY_SEED, 0L);
        int score = preferences.getInt(KEY_SCORE, 0);
        int moves = preferences.getInt(KEY_MOVES, 0);
        int[] colors = deserializeIntArray(preferences.getString(KEY_COLORS, null));
        boolean[] avails = deserializeBooleanArray(preferences.getString(KEY_AVAILS, null));
        Random generator = deserializeRandom(preferences.getString(KEY_RANDOM, null));
        boolean invertedGravity = preferences.getBoolean(KEY_INVERTED_GRAVITY, false);

        SaveGame saveGame = createSaveGame(seed, score, moves, colors, avails, generator, invertedGravity);
        if (saveGame == null) {
            clear(context);
        }
        return saveGame;
    }

    /**
     * Deletes the saved game from {@link SharedPreferences}.
     *
     * @param context the Android context used to access preferences
     */
    public static void clear(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    /**
     * Captures the current game state and writes it into an Android {@link Bundle}.
     * Used by {@code onSaveInstanceState} to survive activity rotation.
     * Does nothing if the state cannot be captured.
     *
     * @param outState the bundle to write into
     * @param grid     the grid model to snapshot
     * @param score    the current player score
     * @param moves    the current move count
     */
    public static void writeToBundle(Bundle outState, Grid grid, int score, int moves) {
        SaveGame saveGame = capture(grid, score, moves);
        if (saveGame == null) {
            return;
        }

        String serializedRandom = serializeRandom(saveGame.getGenerator());
        if (serializedRandom == null) {
            return;
        }

        outState.putLong(KEY_SEED, saveGame.getSeed());
        outState.putInt(KEY_SCORE, saveGame.getScore());
        outState.putInt(KEY_MOVES, saveGame.getMoves());
        outState.putIntArray(KEY_COLORS, saveGame.getColors());
        outState.putBooleanArray(KEY_AVAILS, saveGame.getAvails());
        outState.putString(KEY_RANDOM, serializedRandom);
        outState.putBoolean(KEY_INVERTED_GRAVITY, saveGame.isInvertedGravity());
    }

    /**
     * Reads a game snapshot from an Android {@link Bundle}.
     * Used by {@code onCreate} when the activity is recreated after rotation.
     *
     * @param bundle the bundle previously written by {@link #writeToBundle}
     * @return the restored {@link SaveGame}, or {@code null} if the data is invalid
     */
    public static SaveGame fromBundle(Bundle bundle) {
        long seed = bundle.getLong(KEY_SEED, 0L);
        int score = bundle.getInt(KEY_SCORE, 0);
        int moves = bundle.getInt(KEY_MOVES, 0);
        int[] colors = bundle.getIntArray(KEY_COLORS);
        boolean[] avails = bundle.getBooleanArray(KEY_AVAILS);
        Random generator = deserializeRandom(bundle.getString(KEY_RANDOM));
        boolean invertedGravity = bundle.getBoolean(KEY_INVERTED_GRAVITY, false);

        return createSaveGame(seed, score, moves, colors, avails, generator, invertedGravity);
    }

    /**
     * Creates a {@link SaveGame} by reading the current state of the given grid.
     *
     * @param grid  the grid to snapshot
     * @param score the current player score
     * @param moves the current move count
     * @return a populated {@link SaveGame}, or {@code null} if the grid is invalid
     */
    private static SaveGame capture(Grid grid, int score, int moves) {
        if (grid == null || grid.getGenerator() == null) {
            return null;
        }

        Box[][] table = grid.getTable();
        int rows = table.length;
        int cols = table[0].length;
        int[] colors = new int[rows * cols];
        boolean[] avails = new boolean[rows * cols];
        int index = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                colors[index] = table[i][j].getColor();
                avails[index] = table[i][j].isAvailable();
                index++;
            }
        }

        return new SaveGame(grid.getSeed(), score, moves, colors, avails, grid.getGenerator(), grid.isInvertedGravity());
    }

    /**
     * Validates the provided fields and constructs a {@link SaveGame} if they are consistent.
     *
     * @param seed            the grid seed
     * @param score           the player score
     * @param moves           the move count
     * @param colors          the cell color array
     * @param avails          the cell availability array
     * @param generator       the pseudo-random generator
     * @param invertedGravity the gravity direction flag
     * @return a {@link SaveGame} if all fields are valid, {@code null} otherwise
     */
    private static SaveGame createSaveGame(long seed, int score, int moves, int[] colors, boolean[] avails, Random generator, boolean invertedGravity) {
        if (colors == null || avails == null || generator == null) {
            return null;
        }

        if (colors.length != avails.length || colors.length != GRID_CELL_COUNT) {
            return null;
        }

        return new SaveGame(seed, score, moves, colors, avails, generator, invertedGravity);
    }

    /**
     * Serialises a {@link Random} object to a Base64-encoded string using
     * Java object serialisation.
     *
     * @param generator the generator to serialise
     * @return the Base64 string, or {@code null} if serialisation fails
     */
    private static String serializeRandom(Random generator) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(generator);
            objectOutputStream.close();
            return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
        } catch (IOException exception) {
            return null;
        }
    }

    /**
     * Deserialises a {@link Random} object from a Base64-encoded string.
     *
     * @param encodedRandom the Base64 string produced by {@link #serializeRandom}
     * @return the restored {@link Random}, or {@code null} if deserialisation fails
     */
    private static Random deserializeRandom(String encodedRandom) {
        if (encodedRandom == null || encodedRandom.isEmpty()) {
            return null;
        }

        try {
            byte[] bytes = Base64.decode(encodedRandom, Base64.NO_WRAP);
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object restored = objectInputStream.readObject();
            objectInputStream.close();
            if (restored instanceof Random) {
                return (Random) restored;
            }
        } catch (IOException | ClassNotFoundException | IllegalArgumentException exception) {
            return null;
        }

        return null;
    }

    /**
     * Serialises an {@code int} array to a comma-separated string.
     *
     * @param values the array to serialise
     * @return the comma-separated string representation
     */
    private static String serializeIntArray(int[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(values[i]);
        }
        return builder.toString();
    }

    /**
     * Deserialises a comma-separated string back into an {@code int} array.
     *
     * @param serializedValues the string produced by {@link #serializeIntArray}
     * @return the restored array, or {@code null} if the string is null, empty, or malformed
     */
    private static int[] deserializeIntArray(String serializedValues) {
        if (serializedValues == null || serializedValues.isEmpty()) {
            return null;
        }

        String[] parts = serializedValues.split(",");
        int[] values = new int[parts.length];
        try {
            for (int i = 0; i < parts.length; i++) {
                values[i] = Integer.parseInt(parts[i]);
            }
        } catch (NumberFormatException exception) {
            return null;
        }
        return values;
    }

    /**
     * Serialises a {@code boolean} array to a compact string of {@code '1'} and {@code '0'}
     * characters.
     *
     * @param values the array to serialise
     * @return the compact string representation
     */
    private static String serializeBooleanArray(boolean[] values) {
        StringBuilder builder = new StringBuilder(values.length);
        for (boolean value : values) {
            builder.append(value ? '1' : '0');
        }
        return builder.toString();
    }

    /**
     * Deserialises a compact {@code '1'}/{@code '0'} string back into a {@code boolean} array.
     *
     * @param serializedValues the string produced by {@link #serializeBooleanArray}
     * @return the restored array, or {@code null} if the string is null, empty, or contains
     *         characters other than {@code '0'} and {@code '1'}
     */
    private static boolean[] deserializeBooleanArray(String serializedValues) {
        if (serializedValues == null || serializedValues.isEmpty()) {
            return null;
        }

        boolean[] values = new boolean[serializedValues.length()];
        for (int i = 0; i < serializedValues.length(); i++) {
            char current = serializedValues.charAt(i);
            if (current == '1') {
                values[i] = true;
            } else if (current == '0') {
                values[i] = false;
            } else {
                return null;
            }
        }
        return values;
    }
}
