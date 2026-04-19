package fr.iut_fbleau.chuzzle.controller.listeners;

import android.content.Intent;
import android.view.View;

import fr.iut_fbleau.chuzzle.controller.activities.MainActivity;
import fr.iut_fbleau.chuzzle.controller.activities.SeedActivity;

/**
 * The <code>PlaySeedListener</code> class handles clicks on the "Seeded Game" button
 * in {@link MainActivity}. It navigates to {@link SeedActivity} where the player can
 * enter a numeric seed to start a reproducible game session.
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class PlaySeedListener implements View.OnClickListener {

    /**
     * The main menu activity used as the intent source.
     */
    private MainActivity activity;

    /**
     * Constructs a listener bound to the given main menu activity.
     *
     * @param activity the activity that hosts the seeded game button
     */
    public PlaySeedListener(MainActivity activity) {
        this.activity = activity;
    }

    /**
     * Starts {@link SeedActivity} to let the player enter a seed value.
     *
     * @param v the view that was clicked (the seeded game button)
     */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this.activity, SeedActivity.class);
        this.activity.startActivity(intent);
    }
}
