package com.lfo.partynonsense.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lfo.partynonsense.activitys.GameActivity;

/**
 * Displays info about current game before the game is started.
 *
 * @author Linus Forsberg
 */

public class GameInfoAlertDialogFragment extends DialogFragment {

    private String gameTitle;
    private String gameInfo;
    private int resourceId;
    private ImageView imageView;

    /**
     * Create alert dialog fragment
     *
     * @param savedInstanceState
     * @return
     */
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(gameTitle)
                .setMessage(gameInfo)
                .setPositiveButton("START GAME!", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ((GameActivity) getActivity()).startCountDownTimer();
                    }
                });
        LinearLayout layout = new LinearLayout(getActivity());
        ImageView imageView = new ImageView(getActivity());
        imageView.setImageResource(resourceId);
        imageView.setVisibility(View.VISIBLE);
        layout.addView(imageView);
        builder.setView(layout);
        AlertDialog alertDialog = builder.create();
        setCancelable(false);
        return alertDialog;
    }

    /**
     * set title of Alert dialog
     * @param gameTitle
     */
    public void setTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    /**
     * set info message of Alert dialog
     * @param gameInfo
     */
    public void setText(String gameInfo) {
        this.gameInfo = gameInfo;
    }

    /**
     * set info image resource in
     * @param resourceId
     */
    public void setImageResource(int resourceId) {
        this.resourceId = resourceId;

    }

}
