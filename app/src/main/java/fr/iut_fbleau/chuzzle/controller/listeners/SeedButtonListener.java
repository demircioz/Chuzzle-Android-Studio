package fr.iut_fbleau.chuzzle.controller.listeners;

import android.view.View;
import android.widget.EditText;

import fr.iut_fbleau.chuzzle.R;
import fr.iut_fbleau.chuzzle.controller.activities.SeedActivity;

/**
 * The <code>SeedButtonListener</code> class handles clicks on the play button in
 * {@link SeedActivity}. It validates the text entered in the seed field: the field
 * must not be empty and must contain a valid {@code long} value. If validation
 * passes, it delegates to {@link SeedActivity#lancerLeJeu(long)} to start the game.
 * Otherwise, an inline error is displayed directly on the {@link EditText}.
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class SeedButtonListener implements View.OnClickListener {

    /**
     * The activity used to start the game and to resolve string resources for error messages.
     */
    private SeedActivity activity;

    /**
     * The text field from which the seed value is read and on which errors are displayed.
     */
    private EditText seedEditText;

    /**
     * Constructs a listener bound to the given activity and seed input field.
     *
     * @param activity     the seed activity that hosts this listener
     * @param seedEditText the text field containing the seed value entered by the player
     */
    public SeedButtonListener(SeedActivity activity, EditText seedEditText) {
        this.activity = activity;
        this.seedEditText = seedEditText;
    }

    /**
     * Validates the seed field and either launches the game or shows an inline error.
     * <ul>
     *   <li>If the field is empty, an error message is set on the field.</li>
     *   <li>If the text cannot be parsed as a {@code long}, an invalid-format error is shown.</li>
     *   <li>If parsing succeeds, {@link SeedActivity#lancerLeJeu(long)} is called.</li>
     * </ul>
     *
     * @param v the view that was clicked (the play button)
     */
    @Override
    public void onClick(View v) {
        String seedText = this.seedEditText.getText().toString();

        if (seedText.isEmpty()) {
            this.seedEditText.setError(this.activity.getString(R.string.seed_error_required));
        } else {
            try {
                long seedValue = Long.parseLong(seedText);
                this.activity.lancerLeJeu(seedValue);
            } catch (NumberFormatException e) {
                this.seedEditText.setError(this.activity.getString(R.string.seed_error_invalid));
            }
        }
    }
}
