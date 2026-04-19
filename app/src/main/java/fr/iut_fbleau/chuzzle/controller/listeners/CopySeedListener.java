package fr.iut_fbleau.chuzzle.controller.listeners;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

/**
 * The <code>CopySeedListener</code> class handles clicks on the game-over overlay.
 * When triggered, it copies the finished game's seed value to the system clipboard
 * and shows a short {@link Toast} to confirm the action to the player.
 *
 * <p>The seed can then be pasted into {@link fr.iut_fbleau.chuzzle.controller.activities.SeedActivity}
 * to replay the same board.</p>
 *
 * @version 1.0
 * @author Maxime ELIOT, Canpolat DEMIRCI-ÖZMEN &amp; Adrien RABOT
 */
public class CopySeedListener implements View.OnClickListener {

    /**
     * The Android context used to access the clipboard service and show a Toast.
     */
    private Context context;

    /**
     * The seed value of the finished game to copy to the clipboard.
     */
    private long seed;

    /**
     * Constructs a listener that will copy the given seed to the clipboard.
     *
     * @param context the Android context (typically the game activity)
     * @param seed    the seed value to copy when the overlay is tapped
     */
    public CopySeedListener(Context context, long seed) {
        this.context = context;
        this.seed = seed;
    }

    /**
     * Copies the seed to the system clipboard and shows a confirmation Toast.
     * Does nothing if the clipboard service is unavailable.
     *
     * @param v the view that was clicked (the game-over overlay)
     */
    @Override
    public void onClick(View v) {
        // On récupère le service de presse-papier d'Android
        ClipboardManager clipboard = (ClipboardManager) this.context.getSystemService(Context.CLIPBOARD_SERVICE);

        // On crée l'objet contenant le texte à copier
        ClipData clip = ClipData.newPlainText("Graine Chuzzle", String.valueOf(this.seed));

        // On place l'objet dans le presse-papier (en vérifiant que le service existe)
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }

        // On affiche un petit message temporaire en bas de l'écran
        Toast.makeText(this.context, "Graine copiée !", Toast.LENGTH_SHORT).show();
    }
}
