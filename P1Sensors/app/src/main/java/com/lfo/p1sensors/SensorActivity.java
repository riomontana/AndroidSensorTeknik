package com.lfo.p1sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView tvSensorName;
    private TextView tvSensorVal1;
    private TextView tvSensorVal2;
    private TextView tvSensorVal3;
    private TextView tvAccuracy;
    private TextView tvTimeStamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        tvSensorName = findViewById(R.id.tvSensorName);
        tvSensorVal1 = findViewById(R.id.tvSensorVal1);
        tvSensorVal2 = findViewById(R.id.tvSensorVal2);
        tvSensorVal3 = findViewById(R.id.tvSensorVal3);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvTimeStamp = findViewById(R.id.tvTimeStamp);
        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        Bundle extras = getIntent().getExtras();
        int sensorType = extras.getInt("sensor");

        if (sensorManager.getDefaultSensor(sensorType) != null) {
            sensor = sensorManager.getDefaultSensor(sensorType);
        } else {
            Toast.makeText(SensorActivity.this,
                    "Sorry, the sensor is not available on the device",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        Toast.makeText(SensorActivity.this, sensor.getName() +
                ": sensor listener registered", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        Toast.makeText(SensorActivity.this, sensor.getName() +
                ": sensor listener unregistered", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager = null;
        sensor = null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        tvSensorName.setText(sensor.getName());
        tvSensorVal1.setText("Value 1: " + String.valueOf(sensorEvent.values[0]));
        tvSensorVal2.setText("Value 2: " + String.valueOf(sensorEvent.values[1]));
        tvSensorVal3.setText("Value 3: " + String.valueOf(sensorEvent.values[2]));
        tvAccuracy.setText("Accuracy: " + String.valueOf(sensorEvent.accuracy));
        tvTimeStamp.setText("Timestamp: " + String.valueOf(sensorEvent.timestamp));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
