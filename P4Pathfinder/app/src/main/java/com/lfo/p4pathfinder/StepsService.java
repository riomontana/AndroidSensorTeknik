package com.lfo.p4pathfinder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;

public class StepsService extends Service implements SensorEventListener {

    private LocalBinder binder;
    private DBHelper dbHelper;
    private SensorManager sensorManager;
    private Sensor stepDetector;
    private MainActivity mainActivity;
    private String username;
    private String password;
    private int stepsPerSecond = 0;
    private ArrayList<Long> stepTimeStampList = new ArrayList<>();

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean stopService(Intent name) {
        sensorManager = null;
        stepDetector = null;
        return super.stopService(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = new DBHelper(this);
        binder = new LocalBinder();
        sensorManager = (SensorManager)
                this.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor
                (Sensor.TYPE_STEP_DETECTOR) != null) {
            stepDetector = sensorManager.getDefaultSensor
                    (Sensor.TYPE_STEP_DETECTOR);
            dbHelper = new DBHelper(this);
        } else {
            Toast.makeText(
                    this, "Step detector not available",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        sensorManager.registerListener(this, stepDetector,
                SensorManager.SENSOR_DELAY_NORMAL);

        return binder;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == stepDetector) {
            dbHelper.createStepsEntry(username, password);
            mainActivity.updateNbrOfStepsGui();
            stepTimeStampList.add(sensorEvent.timestamp);
            for (int i = 0; i < stepTimeStampList.size(); i++) {
                if (stepTimeStampList.get(i) - stepTimeStampList.get(0) <= 1000) {
                    stepsPerSecond++;
                } else {
                    mainActivity.setStepsPerSecond(stepsPerSecond);
                    stepTimeStampList.clear();
                    stepsPerSecond = 0;
                }
            }
        }
    }

    public void setListenerActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public class LocalBinder extends Binder {
        StepsService getService() {
            return StepsService.this;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
