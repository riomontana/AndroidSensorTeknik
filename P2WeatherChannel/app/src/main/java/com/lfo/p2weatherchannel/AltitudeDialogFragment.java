package com.lfo.p2weatherchannel;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * DialogFragment is shown to user when Show altitude button is clicked
 * Shows an estimated value of the users altitude
 * Created by LFO on 2018-01-24.
 */

public class AltitudeDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // get saved value of altitude from bundle
        String altitude = (String) getArguments().get("altitude");

        // create alert dialog with title and message
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle("Altitude")
                .setMessage("Your current altitude is:\n" + altitude + " m above sea level");
        AlertDialog alertDialog = alertDialogBuilder.create();
        return alertDialog;
    }

    static AltitudeDialogFragment newInstance() {
        return new AltitudeDialogFragment();
    }
}
