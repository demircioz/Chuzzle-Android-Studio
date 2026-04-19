package fr.iut_fbleau.chuzzle.controller.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import fr.iut_fbleau.chuzzle.R;

/**
 * The <code>OptionsActivity</code> class displays the application settings screen.
 * It extends {@link PreferenceActivity} and loads the preference definitions from
 * {@code R.xml.preferences}, which includes the hard mode toggle.
 *
 * <p>Note: {@link PreferenceActivity} is deprecated as of API level 11 but is used
 * here in accordance with the course guidelines.</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class OptionsActivity extends PreferenceActivity {

    /**
     * Initialises the activity and inflates the preference hierarchy from
     * {@code R.xml.preferences}.
     *
     * @param savedInstanceState the instance state bundle (not used here)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.addPreferencesFromResource(R.xml.preferences);
    }
}
