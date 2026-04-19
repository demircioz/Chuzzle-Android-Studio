package fr.iut_fbleau.chuzzle.controller.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import fr.iut_fbleau.chuzzle.R;
import fr.iut_fbleau.chuzzle.controller.game.GameLoop;
import fr.iut_fbleau.chuzzle.controller.game.GameTouchController;
import fr.iut_fbleau.chuzzle.controller.listeners.CopySeedListener;
import fr.iut_fbleau.chuzzle.controller.persistence.SaveGame;
import fr.iut_fbleau.chuzzle.controller.persistence.SaveGameManager;
import fr.iut_fbleau.chuzzle.model.grid.Grid;
import fr.iut_fbleau.chuzzle.view.GameView;

/**
 * The <code>GameActivity</code> class is the main game screen of the Chuzzle application.
 * It hosts the {@link GameView}, the HUD displaying the current score and move count,
 * and the game-over overlay shown when no more moves are available.
 *
 * <p>A game session can be started in three ways, controlled by the intent extras:</p>
 * <ul>
 *   <li>Random game — no extras (grid seeded from the current time).</li>
 *   <li>Seeded game — {@link #EXTRA_USE_SEED} set to {@code true} and
 *       {@link #EXTRA_SEED_VALUE} set to the desired seed.</li>
 *   <li>Resumed game — {@link #EXTRA_RESUME_SAVED_GAME} set to {@code true},
 *       restoring the state persisted by {@link SaveGameManager}.</li>
 * </ul>
 *
 * <p>The game state is automatically saved on pause and restored on resume,
 * including instance state restoration on screen rotation.</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class GameActivity extends Activity {

    /**
     * Intent extra key (boolean): when {@code true}, the game uses the seed
     * provided by {@link #EXTRA_SEED_VALUE}.
     */
    public static final String EXTRA_USE_SEED = "USE_SEED";

    /**
     * Intent extra key (long): the seed value to use when {@link #EXTRA_USE_SEED}
     * is {@code true}.
     */
    public static final String EXTRA_SEED_VALUE = "SEED_VALUE";

    /**
     * Intent extra key (boolean): when {@code true}, the activity restores the
     * game state persisted by {@link SaveGameManager}.
     */
    public static final String EXTRA_RESUME_SAVED_GAME = "RESUME_SAVED_GAME";

    /**
     * The grid model holding the current board state.
     */
    private Grid grid;

    /**
     * The custom view that renders the game board.
     */
    private GameView gameView;

    /**
     * The game loop that processes turns, scoring, and lock events.
     */
    private GameLoop gameLoop;

    /**
     * Whether the game is currently over (no more valid moves).
     * When {@code true}, touch input is disabled and the game-over overlay is shown.
     */
    private boolean isGameOver = false;

    /**
     * TextView (portrait) showing the live score in the top HUD.
     */
    private TextView scoreTextView;

    /**
     * TextView (portrait) showing the live move count in the top HUD.
     */
    private TextView movesTextView;

    /**
     * The top HUD layout, hidden at game over in portrait mode.
     */
    private View hudLayout;

    /**
     * The game-over overlay layout shown at the end of the game.
     */
    private LinearLayout gameOverLayout;

    /**
     * TextView in the game-over overlay showing a summary (hidden in portrait mode).
     */
    private TextView gameOverStats;

    /**
     * TextView in the game-over overlay showing the seed of the finished game.
     */
    private TextView gameOverSeed;

    /**
     * The bottom stats panel shown at game over in portrait mode.
     */
    private View bottomStatsLayout;

    /**
     * TextView in the bottom stats panel showing the final score.
     */
    private TextView bottomScoreText;

    /**
     * TextView in the bottom stats panel showing the final move count.
     */
    private TextView bottomMovesText;

    /**
     * The player's current score, updated after every valid move.
     */
    private int currentScore = 0;

    /**
     * The number of moves played since the game started.
     */
    private int movesCount = 0;

    /**
     * Initialises the activity: inflates the layout, resolves views, reads the
     * hard mode preference, restores or creates the grid, sets up the game loop
     * and touch controller, and updates the HUD.
     *
     * @param savedInstanceState the instance state bundle written by
     *                           {@link #onSaveInstanceState}, or {@code null} on a fresh start
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        this.setContentView(R.layout.activity_game);

        this.scoreTextView = this.findViewById(R.id.scoreTextView);
        this.movesTextView = this.findViewById(R.id.movesTextView);
        this.hudLayout = this.findViewById(R.id.hudLayout);
        this.gameView = this.findViewById(R.id.gameView);

        this.gameOverLayout = this.findViewById(R.id.gameOverLayout);
        this.gameOverStats = this.findViewById(R.id.gameOverStats);
        this.gameOverSeed = this.findViewById(R.id.gameOverSeed);

        this.bottomStatsLayout = this.findViewById(R.id.bottomStatsLayout);
        this.bottomScoreText = this.findViewById(R.id.bottomScoreText);
        this.bottomMovesText = this.findViewById(R.id.bottomMovesText);

        android.content.SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isHardMode = prefs.getBoolean("pref_hard_mode", false);

        SaveGame saveGame = null;
        if (savedInstanceState != null) {
            saveGame = SaveGameManager.fromBundle(savedInstanceState);
        } else if (this.getIntent().getBooleanExtra(EXTRA_RESUME_SAVED_GAME, false)) {
            saveGame = SaveGameManager.load(this);
        }

        if (saveGame != null) {
            this.restoreSaveGame(saveGame);
        } else {
            this.createGridFromIntent();
        }

        this.gameView.setGrid(this.grid);
        this.updateHud();

        this.gameLoop = new GameLoop(this.grid, this.gameView, this);
        this.gameLoop.setHardMode(isHardMode);
        GameTouchController touchController = new GameTouchController(this.gameView, this.gameLoop);
        this.gameView.setOnTouchListener(touchController);
    }

    /**
     * Saves the game state to the instance state bundle so it survives screen rotation.
     * The state is only saved if the game is not over and the grid is not currently
     * being resolved.
     *
     * @param outState the bundle to write the game state into
     */
    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.canPersistCurrentGame()) {
            SaveGameManager.writeToBundle(outState, this.grid, this.currentScore, this.movesCount);
        }
    }

    /**
     * Persists the current game state to {@link SaveGameManager} when the activity
     * is paused, so it can be resumed later from the main menu.
     */
    @Override
    protected void onPause() {
        if (this.canPersistCurrentGame()) {
            this.persistCurrentGame();
        }
        super.onPause();
    }

    /**
     * Handles the back button by navigating back to {@link MainActivity}
     * without adding a new back-stack entry.
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        this.startActivity(intent);
        this.finish();
    }

    /**
     * Adds the given number of points to the player's score and refreshes the HUD.
     * Called by {@link GameLoop} after each valid move.
     *
     * @param pointsEarned the points to add (must be non-negative)
     */
    public void addScore(int pointsEarned) {
        this.currentScore += pointsEarned;
        this.updateHud();
    }

    /**
     * Increments the move counter by one and refreshes the HUD.
     * Called by {@link GameLoop} at the start of each valid move.
     */
    public void incrementMoves() {
        this.movesCount++;
        this.updateHud();
    }

    /**
     * Returns the number of moves played so far.
     *
     * @return the move count
     */
    public int getMovesCount() {
        return this.movesCount;
    }

    /**
     * Updates the score and move count text views in both the top HUD
     * and the bottom stats panel (if present in the current layout).
     */
    private void updateHud() {
        this.scoreTextView.setText(this.getString(R.string.game_score_value, this.currentScore));
        this.movesTextView.setText(this.getString(R.string.game_moves_value, this.movesCount));

        if (this.bottomScoreText != null) {
            this.bottomScoreText.setText(this.getString(R.string.game_final_score_format, this.currentScore));
        }
        if (this.bottomMovesText != null) {
            this.bottomMovesText.setText(this.getString(R.string.game_final_moves_format, this.movesCount));
        }
    }

    /**
     * Ends the game: disables touch input, hides the live HUD, shows the game-over
     * overlay with the final seed, and clears the persisted save.
     * Tapping the overlay copies the seed to the clipboard.
     * Called by {@link GameLoop} when no valid move remains.
     */
    public void gameOver() {
        this.isGameOver = true;
        SaveGameManager.clear(this);
        long seed = this.grid.getSeed();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (this.hudLayout != null) {
                this.hudLayout.setVisibility(View.GONE);
            }
            if (this.bottomStatsLayout != null) {
                this.bottomStatsLayout.setVisibility(View.VISIBLE);
                this.bottomScoreText.setText(this.getString(R.string.game_final_score_format, this.currentScore));
                this.bottomMovesText.setText(this.getString(R.string.game_final_moves_format, this.movesCount));
            }
            if (this.gameOverStats != null) {
                this.gameOverStats.setVisibility(View.GONE);
            }
        } else {
            if (this.gameOverStats != null) {
                this.gameOverStats.setVisibility(View.GONE);
            }
        }

        String seedText = this.getString(R.string.game_seed_display, seed);
        this.gameOverSeed.setText(seedText);
        this.gameOverLayout.setVisibility(View.VISIBLE);
        this.gameView.setOnTouchListener(null);

        CopySeedListener copyListener = new CopySeedListener(this, seed);
        this.gameOverLayout.setOnClickListener(copyListener);
    }

    /**
     * Saves the current game state to {@link SharedPreferences} via {@link SaveGameManager}.
     * Called by {@link GameLoop} after each valid move and by {@link #onPause}.
     */
    public void persistCurrentGame() {
        SaveGameManager.save(this, this.grid, this.currentScore, this.movesCount);
    }

    /**
     * Returns whether the current game state can be safely persisted.
     * Persistence is blocked if the game is over, the grid is null, or
     * the game loop is currently resolving a turn.
     *
     * @return {@code true} if the state can be saved
     */
    private boolean canPersistCurrentGame() {
        return !this.isGameOver && this.grid != null && (this.gameLoop == null || !this.gameLoop.isResolvingTurn());
    }

    /**
     * Creates a new grid from the intent extras: either a seeded grid
     * (when {@link #EXTRA_USE_SEED} is {@code true}) or a random grid.
     */
    private void createGridFromIntent() {
        Intent intent = this.getIntent();
        boolean useSeed = intent.getBooleanExtra(EXTRA_USE_SEED, false);

        if (useSeed) {
            long seedValue = intent.getLongExtra(EXTRA_SEED_VALUE, 0);
            this.grid = new Grid(seedValue);
        } else {
            this.grid = new Grid();
        }
    }

    /**
     * Restores the grid and score/move counters from a previously saved game snapshot.
     *
     * @param saveGame the snapshot to restore from
     */
    private void restoreSaveGame(SaveGame saveGame) {
        this.currentScore = saveGame.getScore();
        this.movesCount = saveGame.getMoves();
        this.grid = new Grid(saveGame.getSeed());
        this.grid.restoreState(
                saveGame.getColors(),
                saveGame.getAvails(),
                saveGame.getGenerator(),
                saveGame.isInvertedGravity()
        );
    }
}
