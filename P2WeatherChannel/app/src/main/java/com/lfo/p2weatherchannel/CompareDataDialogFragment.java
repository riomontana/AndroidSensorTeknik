package com.lfo.p2weatherchannel;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import java.text.DecimalFormat;

/**
 * DialogFragment shown when user clicks on Compare data button
 * Shows a comparison between values from sensor and Api data
 * Created by LFO on 2018-02-09.
 */

public class CompareDataDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Shorten decimals to two only.
        DecimalFormat df = new DecimalFormat("#.##");

        // get saved values from bundle
        double tempDif = (double) getArguments().get("tempDif");
        double pressureDif = (double) getArguments().get("pressureDif");
        double humidityDif = (double) getArguments().get("humidityDif");

        // create alert dialog with title and message to user
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle("Difference between sensor readings and API data:")
                .setMessage("Temperature: "  + df.format(tempDif) + " °C" +
                        "\nPressure: " + df.format(pressureDif) + " hpa" +
                        "\nHumidity: " + df.format(humidityDif) + "%");
        AlertDialog alertDialog = alertDialogBuilder.create();

        return alertDialog;
    }

    static CompareDataDialogFragment newInstance() {
        return new CompareDataDialogFragment();
    }
}
