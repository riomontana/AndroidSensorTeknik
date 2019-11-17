package com.lfo.p2weatherchannel;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Main Activity for application
 * Shows weather information from built in sensors and from OpenWeather API
 * Also shows the estimated altitude and a comparison between sensor readings and API data
 * User can choose from getting API data from AsyncTask or Volley
 * Extends AppCompatActivity
 * Implements SensorEventListener and custom interface RetrieveFinished
 */
public class WeatherActivity extends AppCompatActivity implements SensorEventListener, RetrieveFinished {

    private SensorManager sensorManager;
    private FragmentManager fm = getSupportFragmentManager();
    private Sensor sensorTemp, sensorPressure, sensorHumidity;
    private List<Sensor> sensorList = new ArrayList<>();
    private TextView tvTitle, tvCity, tvSensorTitle, tvApiTitle;
    private TextView tvSensorTemp, tvSensorPressure, tvSensorHumidity, tvSensorTimeStamp;
    private TextView tvApiTemp, tvApiPressure, tvApiHumidity;
    private RadioButton rbAsync, rbVolley;
    private Button btnShowAltitude, btnCompareData;
    private String city = "Malmö";
    private String key = "92d45b077fa249614bfc79c61cf8b50f";
    private String urlString = "http://api.openweathermap.org/data/2.5/weather?q=" +
            city + "&units=metric&APPID=";
    private float currentPressure;
    private float seaLevelPressure;
    private double sensorTempValue, sensorPressureValue, sensorHumidityValue;
    private double apiTempValue, apiPressureValue, apiHumidityValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // get sensor manager
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        // check if sensors exist in the device
        checkSensors();
        // adds sensors to an array list
        addSensorsToList();
        // initialize GUI components
        initComponents();

    }

    /**
     * Checks if sensors exist in the device and informs user if not.
     */
    private void checkSensors() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            sensorTemp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        } else {
            Toast.makeText(WeatherActivity.this,
                    "Sorry, the temperature sensor is not available on this device",
                    Toast.LENGTH_SHORT).show();
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            sensorPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        } else {
            Toast.makeText(WeatherActivity.this,
                    "Sorry, the pressure sensor is not available on this device",
                    Toast.LENGTH_SHORT).show();
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null) {
            sensorHumidity = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        } else {
            Toast.makeText(WeatherActivity.this,
                    "Sorry, the humidity sensor is not available on this device",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Adds sensors to array list
     */
    private void addSensorsToList() {
        sensorList.add(sensorTemp);
        sensorList.add(sensorHumidity);
        sensorList.add(sensorPressure);
    }

    /**
     * Initialize GUI components
     */
    private void initComponents() {
        tvTitle = findViewById(R.id.tvTitle);
        tvCity = findViewById(R.id.tvCity);
        tvApiTitle = findViewById(R.id.tvApiTitle);
        tvApiHumidity = findViewById(R.id.tvApiHumidity);
        tvApiPressure = findViewById(R.id.tvApiPressure);
        tvApiTemp = findViewById(R.id.tvApiTemp);
        tvSensorTitle = findViewById(R.id.tvSensorTitle);
        tvSensorTemp = findViewById(R.id.tvSensorTemp);
        tvSensorPressure = findViewById(R.id.tvSensorPressure);
        tvSensorHumidity = findViewById(R.id.tvSensorHumidity);
        tvSensorTimeStamp = findViewById(R.id.tvSensorTimeStamp);
        btnCompareData = findViewById(R.id.btnCompareData);
        btnShowAltitude = findViewById(R.id.btnShowAltitude);
        rbAsync = findViewById(R.id.rbAsync);
        rbVolley = findViewById(R.id.rbVolley);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Register sensor listeners to sensors and notify user
        for (Sensor s : sensorList) {
            sensorManager.registerListener(this, s,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        Toast.makeText(WeatherActivity.this,
                "Sensor listeners registered", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Unregister sensor listeners and notify user
        sensorManager.unregisterListener(this);
        Toast.makeText(WeatherActivity.this,
                "Sensor listeners unregistered", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // set sensorManager and all sensor array list to null for garbage collection
        sensorManager = null;
        sensorList = null;
    }

    /**
     * Gets API data from OpenWeatherApi using volley
     * Method is called on click on radio button if user wants API data from volley
     */
    private void volleyRequest() {
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlString + key,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        processJSON(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR", error.getMessage(), error);
            }
        });
        queue.add(stringRequest);
    }

    /**
     * Gets called from AsyncTask when a response has arrived from API server
     * Sends the retrieved message to processing and GUI
     *
     * @param output retrieved data
     */
    @Override
    public void retrieveFinish(String output) {
        processJSON(output);
    }

    /**
     * Process the data response from OpenWeatherApi and shows it to user
     *
     * @param response data from API
     */
    private void processJSON(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject != null) {
                Log.d("Json response:", jsonObject.toString());
                JSONObject main;
                main = jsonObject.getJSONObject("main");
                String strTemperature = main.getString("temp");
                String strPressure = main.getString("pressure");
                String strHumidity = main.getString("humidity");
                tvApiTemp.setText("Temperature:\n" + strTemperature + " °C");
                tvApiHumidity.setText("Humidity:\n" + strHumidity + "%");
                tvApiPressure.setText("Pressure\n" + strPressure + " hPa");
                seaLevelPressure = Float.parseFloat(strPressure);
                apiTempValue = Double.valueOf(strTemperature);
                apiHumidityValue = Double.valueOf(strHumidity);
                apiPressureValue = Double.valueOf(strPressure);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles radio button clicks.
     * Executes AsyncTask or makes a volley request
     * for fetching data from API depending on user choice
     * @param view
     */
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.rbAsync:
                if (checked) {
                    new RetrieveJsonTask(this).execute();
                    Toast.makeText(WeatherActivity.this,
                            "API data received with AsyncTask",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rbVolley:
                if (checked) {
                    volleyRequest();
                    Toast.makeText(WeatherActivity.this,
                            "API data received with Volley",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    /**
     * Handles button click
     * Shows a dialog fragment with information about current altitude
     * @param view
     */
    public void onAltitudeButtonClick(View view) {
        String altitude =
                String.valueOf(Math.round(SensorManager.getAltitude(seaLevelPressure, currentPressure)));
        Bundle args = new Bundle();
        args.putString("altitude", altitude);
        DialogFragment dialogFragment = AltitudeDialogFragment.newInstance();
        dialogFragment.setArguments(args);
        dialogFragment.show(fm, "altitude dialog");
    }

    /**
     * Shows a dialog fragment with data comparing the values from sensors and API
     * @param view
     */
    public void onCompareDataButtonClick(View view) {
        double tempDif = Math.abs(sensorTempValue - apiTempValue);
        double pressureDif = Math.abs(sensorPressureValue - apiPressureValue);
        double humidityDif = Math.abs(sensorHumidityValue - apiHumidityValue);
        Bundle args = new Bundle();
        args.putDouble("tempDif", tempDif);
        args.putDouble("pressureDif", pressureDif);
        args.putDouble("humidityDif", humidityDif);
        DialogFragment dialogFragment = CompareDataDialogFragment.newInstance();
        dialogFragment.setArguments(args);
        dialogFragment.show(fm, "compare fragment");
    }

    /**
     * Handles sensor events for temperature, humidity and pressure
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        tvSensorTimeStamp.setText("Timestamp:\n" + String.valueOf(sensorEvent.timestamp));
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                //values[0]: ambient (room) temperature in degree Celsius.
                tvSensorTemp.setText("Temperature:\n" + sensorEvent.values[0] + " °C");
                sensorTempValue = Double.valueOf(sensorEvent.values[0]);
                break;

            case Sensor.TYPE_RELATIVE_HUMIDITY:
                //values[0]: Relative ambient air humidity in percent
                tvSensorHumidity.setText("Humidity:\n" + sensorEvent.values[0] + "%");
                sensorHumidityValue = Double.valueOf(sensorEvent.values[0]);
                break;

            case Sensor.TYPE_PRESSURE:
                //values[0]: Atmospheric pressure in hPa (millibar)
                currentPressure = sensorEvent.values[0];
                tvSensorPressure.setText("Pressure:\n" + sensorEvent.values[0] + " hPa");
                sensorPressureValue = Double.valueOf(sensorEvent.values[0]);
                break;
        }
    }

    /**
     * Unused method
     * @param sensor
     * @param i
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}