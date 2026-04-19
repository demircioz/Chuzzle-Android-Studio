package fr.iut_fbleau.chuzzle.controller.listeners;

import android.content.Intent;
import android.view.View;

import fr.iut_fbleau.chuzzle.controller.activities.MainActivity;
import fr.iut_fbleau.chuzzle.controller.activities.GameActivity;

/**
 * The <code>PlayRandomListener</code> class handles clicks on the "Play" button in
 * {@link MainActivity}. It starts {@link GameActivity} without any seed extras,
 * causing the game to generate a random board seeded from the current time.
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class PlayRandomListener implements View.OnClickListener {

    /**
     * The main menu activity used as the intent source.
     */
    private MainActivity activity;

    /**
     * Constructs a listener bound to the given main menu activity.
     *
     * @param activity the activity that hosts the play button
     */
    public PlayRandomListener(MainActivity activity) {
        this.activity = activity;
    }

    /**
     * Starts {@link GameActivity} with no seed extras, launching a random game.
     *
     * @param v the view that was clicked (the play button)
     */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this.activity, GameActivity.class);
        this.activity.startActivity(intent);
    }
}
