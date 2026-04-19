package fr.iut_fbleau.chuzzle.controller.listeners;

import android.content.Intent;
import android.view.View;

import fr.iut_fbleau.chuzzle.controller.activities.MainActivity;
import fr.iut_fbleau.chuzzle.controller.activities.GameActivity;

/**
 * The <code>ResumeGameListener</code> class handles clicks on the "Resume" button in
 * {@link MainActivity}. It starts {@link GameActivity} with the
 * {@link GameActivity#EXTRA_RESUME_SAVED_GAME} extra set to {@code true}, instructing
 * the game to restore the state previously persisted by
 * {@link fr.iut_fbleau.chuzzle.controller.persistence.SaveGameManager}.
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class ResumeGameListener implements View.OnClickListener {

    /**
     * The main menu activity used as the intent source.
     */
    private MainActivity activity;

    /**
     * Constructs a listener bound to the given main menu activity.
     *
     * @param activity the activity that hosts the resume button
     */
    public ResumeGameListener(MainActivity activity) {
        this.activity = activity;
    }

    /**
     * Starts {@link GameActivity} with the resume flag set, restoring the last saved game.
     *
     * @param v the view that was clicked (the resume button)
     */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this.activity, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_RESUME_SAVED_GAME, true);
        this.activity.startActivity(intent);
    }
}
