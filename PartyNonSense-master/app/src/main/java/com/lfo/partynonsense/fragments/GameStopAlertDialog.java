package com.lfo.partynonsense.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.lfo.partynonsense.activitys.GameActivity;

/**
 * Alert dialog that is displayed between games
 *
 * @author Linus Forsberg
 */

public class GameStopAlertDialog extends DialogFragment {

    private String gameTitle;
    private int score;

    /**
     * Create alert dialog fragment
     *
     * @param savedInstanceState
     * @return
     */
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(gameTitle)
                .setMessage("Time is up! You scored " + score + " points!")
                .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((GameActivity) getActivity()).changeGame();
                    }
                });
        AlertDialog alertDialog = builder.create();
        setCancelable(false);
        return alertDialog;
    }

    /**
     * Set game title
     */
    public void setTitle(String title) {
        this.gameTitle = title;
    }

    /**
     * Set game score
     * @param score
     */
    public void setScore(int score) {
        this.score = score;
    }

    public void onBackPressed() {
    }
}

