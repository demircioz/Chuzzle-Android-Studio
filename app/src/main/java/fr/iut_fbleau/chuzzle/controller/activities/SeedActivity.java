package fr.iut_fbleau.chuzzle.controller.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import fr.iut_fbleau.chuzzle.R;
import fr.iut_fbleau.chuzzle.controller.listeners.SeedButtonListener;

/**
 * The <code>SeedActivity</code> class lets the player enter a numeric seed to start
 * a reproducible game session. It hosts a text field for the seed value and a
 * confirmation button that validates the input before launching {@link GameActivity}.
 *
 * <p>Back-navigation is handled explicitly to return to {@link MainActivity}
 * without leaving orphan entries on the back stack.</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class SeedActivity extends Activity {

    /**
     * The text field in which the player types the desired seed value.
     */
    private EditText seedEditText;

    /**
     * The button that triggers seed validation and game launch.
     */
    private Button playSeedButton;

    /**
     * Initialises the activity: inflates the layout, resolves views, and attaches
     * a {@link SeedButtonListener} to the play button.
     *
     * @param savedInstanceState the instance state bundle (not used here)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_seed);

        this.seedEditText = this.findViewById(R.id.seedEditText);
        this.playSeedButton = this.findViewById(R.id.playSeedButton);

        SeedButtonListener ecouteur = new SeedButtonListener(this, this.seedEditText);
        this.playSeedButton.setOnClickListener(ecouteur);
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
     * Starts {@link GameActivity} with the given seed and finishes this activity.
     * Called by {@link SeedButtonListener} once the seed has been validated.
     *
     * @param seed the numeric seed to pass to the game
     */
    public void lancerLeJeu(long seed) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_USE_SEED, true);
        intent.putExtra(GameActivity.EXTRA_SEED_VALUE, seed);
        this.startActivity(intent);
        this.finish();
    }
}
