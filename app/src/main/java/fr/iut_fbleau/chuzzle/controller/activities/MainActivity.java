package fr.iut_fbleau.chuzzle.controller.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import fr.iut_fbleau.chuzzle.R;
import fr.iut_fbleau.chuzzle.controller.listeners.PlayRandomListener;
import fr.iut_fbleau.chuzzle.controller.listeners.PlaySeedListener;
import fr.iut_fbleau.chuzzle.controller.listeners.ResumeGameListener;
import fr.iut_fbleau.chuzzle.controller.listeners.OptionsListener;
import fr.iut_fbleau.chuzzle.controller.persistence.SaveGameManager;

/**
 * The <code>MainActivity</code> class is the entry point of the Chuzzle application.
 * It presents the main menu, which offers four actions:
 * <ul>
 *   <li>Start a random game.</li>
 *   <li>Start a seeded game (navigates to {@link SeedActivity}).</li>
 *   <li>Resume the last saved game (button only visible when a save exists).</li>
 *   <li>Open the settings screen (accessible via the action bar overflow menu).</li>
 * </ul>
 *
 * <p>Default preference values are initialised here on every start to ensure
 * they are available before any game session begins.</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class MainActivity extends AppCompatActivity {

    /**
     * The "Resume" button, only visible when a saved game is available.
     */
    private Button resumeGameButton;

    /**
     * Initialises the activity: inflates the layout, sets up the action bar,
     * registers button listeners, and initialises default preference values.
     *
     * @param savedInstanceState the instance state bundle (not used here)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        Toolbar toolbar = this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);

        this.resumeGameButton = this.findViewById(R.id.main_menu_resume_button);
        Button playRandomButton = this.findViewById(R.id.main_menu_play_button);
        Button playSeedMenuButton = this.findViewById(R.id.main_menu_seed_button);

        ResumeGameListener resumeGameListener = new ResumeGameListener(this);
        this.resumeGameButton.setOnClickListener(resumeGameListener);

        PlayRandomListener randomListener = new PlayRandomListener(this);
        playRandomButton.setOnClickListener(randomListener);

        PlaySeedListener seedListener = new PlaySeedListener(this);
        playSeedMenuButton.setOnClickListener(seedListener);

        this.updateResumeButtonVisibility();
    }

    /**
     * Refreshes the visibility of the "Resume" button each time the activity
     * returns to the foreground, in case a saved game was created or deleted
     * while the menu was in the background.
     */
    @Override
    protected void onResume() {
        super.onResume();
        this.updateResumeButtonVisibility();
    }

    /**
     * Inflates the action bar menu and attaches a listener to the settings icon button
     * if the menu item provides a custom action view.
     *
     * @param menu the menu to populate
     * @return {@code true} to display the menu
     */
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_main, menu);

        android.view.MenuItem settingsItem = menu.findItem(R.id.action_parametres);
        View actionView = settingsItem.getActionView();
        if (actionView != null) {
            ImageButton settingsButton = actionView.findViewById(R.id.action_settings_button);
            if (settingsButton != null) {
                OptionsListener optionsListener = new OptionsListener(this);
                settingsButton.setOnClickListener(optionsListener);
            }
        }

        return true;
    }

    /**
     * Handles action bar menu item selections. Navigates to {@link OptionsActivity}
     * when the settings item is selected.
     *
     * @param item the selected menu item
     * @return {@code true} if the event was handled, otherwise delegates to the superclass
     */
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_parametres) {
            Intent intent = new Intent(this, OptionsActivity.class);
            this.startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows the "Resume" button if a saved game exists in {@link SaveGameManager},
     * hides it otherwise.
     */
    private void updateResumeButtonVisibility() {
        if (SaveGameManager.load(this) != null) {
            this.resumeGameButton.setVisibility(View.VISIBLE);
        } else {
            this.resumeGameButton.setVisibility(View.GONE);
        }
    }
}
