package com.lfo.p2weatherchannel;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Retreives Json messages from OpenWeatherApi using AsyncTask.
 * Created by LFO on 2018-01-24.
 */

public class RetrieveJsonTask extends AsyncTask<String,Void,String> {

    public WeatherActivity delegate;
    private String city = "Malmö";
    private String key = "92d45b077fa249614bfc79c61cf8b50f";
    private String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" +
            city + "&units=metric&APPID=";


    public RetrieveJsonTask(WeatherActivity delegate) {
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            URL url = new URL(urlString + key);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder jsonString = new StringBuilder();
            String tempStr;

            while ((tempStr = br.readLine()) != null) {
                jsonString.append(tempStr).append("\n");
            }
            br.close();
            return jsonString.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onPostExecute(String response) {
        if(response == null) {
            response = "Error"; }
        delegate.retrieveFinish(response);
    }
}
