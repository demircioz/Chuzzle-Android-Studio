package fr.iut_fbleau.chuzzle.controller.listeners;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import fr.iut_fbleau.chuzzle.controller.activities.OptionsActivity;

/**
 * The <code>OptionsListener</code> class handles clicks on the settings icon in the
 * action bar of {@link fr.iut_fbleau.chuzzle.controller.activities.MainActivity}.
 * It starts {@link OptionsActivity} to display the application preferences screen.
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class OptionsListener implements View.OnClickListener {

    /**
     * The Android context used as the intent source.
     */
    private Context context;

    /**
     * Constructs a listener bound to the given context.
     *
     * @param context the context from which the settings activity will be started
     */
    public OptionsListener(Context context) {
        this.context = context;
    }

    /**
     * Starts {@link OptionsActivity} to display the application settings.
     *
     * @param v the view that was clicked (the settings icon button)
     */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this.context, OptionsActivity.class);
        this.context.startActivity(intent);
    }
}
