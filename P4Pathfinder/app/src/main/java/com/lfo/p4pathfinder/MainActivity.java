package com.lfo.p4pathfinder;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView tvStepCounter;
    private ImageView ivCompass;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float[] lastAccelerometerVal = new float[3];
    private float[] lastMagnetometerVal = new float[3];
    private boolean lastMagnetometerSet = false;
    private boolean lastAccelerometerSet = false;
    private long lastTimeUpdateCompass;
    private float[] rotationMatrix = new float[9];
    private float[] orientation = new float[3];
    private float currentDegree = 0f;
    private int nbrOfSteps;
    private boolean isFirstValue;
    private float last_x;
    private float last_y;
    private float last_z;
    private float shakeThreshold = 20;
    private MyServiceConnection connection;
    private Intent stepsIntent;
    private StepsService service;
    private boolean bound;
    private DBHelper dbHelper;
    private Vibrator v;
    private String username;
    private String password;
    private TextView tvUsername;
    private TextView tvStepsPerSecond;
    private int stepsPerSecond = 0;
    private long shakeRotationTime;
    private boolean continueCompassAnimation = true;
    private Random random = new Random();
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            tvStepsPerSecond.setText(String.valueOf(stepsPerSecond));
            if (stepsPerSecond > 0) {
                stepsPerSecond--;
            }
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = getIntent().getStringExtra("username");
        password = getIntent().getStringExtra("password");
        tvUsername = findViewById(R.id.tvUsername);
        tvUsername.setText("Logged in as " + username);
        tvStepCounter = findViewById(R.id.tvStepCounter);
        tvStepsPerSecond = findViewById(R.id.tvStepsPerSec);
        ivCompass = findViewById(R.id.ivCompass);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        setupSensors();
        dbHelper = new DBHelper(this);
        connection = new MyServiceConnection(this);
    }

    private void setupSensors() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            Toast.makeText(this, "Accelerometer not available", Toast.LENGTH_SHORT).show();
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        } else {
            Toast.makeText(this, "Magnetometer not available", Toast.LENGTH_SHORT).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            this.isFirstValue = false;
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        timerHandler.removeCallbacks(timerRunnable);
    }

    protected void onDestroy() {
        super.onDestroy();
        sensorManager = null;
        accelerometer = null;
        magnetometer = null;
        if (bound) {
            unbindService(connection);
            bound = false;
            Toast.makeText(this,
                    "Step detector, service unbound", Toast.LENGTH_SHORT).show();
        }
    }

    public void startStepService(View view) {
        stepsIntent = new Intent(this, StepsService.class);
        if (!isMyServiceRunning(StepsService.class)) {
            this.startService(stepsIntent);
            Toast.makeText(this, "Step detector, service started", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Step detector, service already running", Toast.LENGTH_SHORT).show();
        }
        if (!bound) {
            bindService(stepsIntent, connection, Context.BIND_AUTO_CREATE);
            bound = true;
            Toast.makeText(this, "Step detector, service bound", Toast.LENGTH_SHORT).show();
        }
        timerHandler.post(timerRunnable);
    }

    public void stopStepService(View view) {
        if (isMyServiceRunning(StepsService.class)) {
            if (bound) {
                unbindService(connection);
                bound = false;
                Toast.makeText(this,
                        "Step detector, service unbound", Toast.LENGTH_SHORT).show();
            }
            if (stepsIntent == null) {
                stepsIntent = new Intent(this, StepsService.class);
            }
            stopService(stepsIntent);
            Toast.makeText(this, "Step detector, service stopped", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Step detector, service is not running", Toast.LENGTH_SHORT).show();
        }
        timerHandler.removeCallbacks(timerRunnable);
        nbrOfSteps = 0;
        tvStepCounter.setText(String.valueOf(nbrOfSteps));
        stepsPerSecond = 0;
        tvStepsPerSecond.setText(String.valueOf(stepsPerSecond));
    }

    public void showStepHistory(View view) {
        ListView lvStepsList = new ListView(MainActivity.this);
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_list_item_1, getStepData());
        lvStepsList.setAdapter(arrayAdapter);
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Step history for " + username)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
        builder.setView(lvStepsList);
        builder.show();
    }

    public void deleteStepHistory(View view) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete step history")
                .setMessage("Are you sure you want to delete the step history?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        nbrOfSteps = 0;
                        tvStepCounter.setText(String.valueOf(nbrOfSteps));
                        dbHelper.deleteUserStepHistory(username);
                        Toast.makeText(MainActivity.this,
                                "Step history deleted.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).show();

    }

    private ArrayList<String> getStepData() {
        ArrayList<DateStepsModel> stepHistoryList;
        ArrayList<String> printList = new ArrayList<>();
        stepHistoryList = dbHelper.readStepsEntries(username);
        for (int i = stepHistoryList.size() - 1; i >= 0; i--) {
            DateStepsModel stepEntry = stepHistoryList.get(i);
            if (stepHistoryList.size() <= 1 && stepEntry.date == null) {
                printList.add("No step history is recorded");
            } else if (stepEntry.stepCount > 0) {
                printList.add("Date: " + stepEntry.date +
                        " - " + stepEntry.stepCount + " steps");
            }
        }
        return printList;
    }

    public void updateNbrOfStepsGui() {
        nbrOfSteps++;
        tvStepCounter.setText(String.valueOf(nbrOfSteps));
    }

    public void setStepsPerSecond(int stepsPerSecond) {
        this.stepsPerSecond = stepsPerSecond;
        tvStepsPerSecond.setText(String.valueOf(stepsPerSecond));
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        shakeRotation(sensorEvent);
        compassRotation(sensorEvent);

    }

    private void compassRotation(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == accelerometer) {
            System.arraycopy(sensorEvent.values,
                    0, lastAccelerometerVal, 0, sensorEvent.values.length);
            lastAccelerometerSet = true;
        }
        if (sensorEvent.sensor == magnetometer) {
            System.arraycopy(sensorEvent.values, 0,
                    lastMagnetometerVal, 0, sensorEvent.values.length);
            lastMagnetometerSet = true;
        }
        if (lastMagnetometerSet && lastAccelerometerSet
                && System.currentTimeMillis() - lastTimeUpdateCompass > 200) {
            SensorManager.getRotationMatrix(rotationMatrix, null,
                    lastAccelerometerVal, lastMagnetometerVal);
            SensorManager.getOrientation(rotationMatrix, orientation);
            float azimuthInRadians = orientation[0];
            float azimuthInDegrees = (float)
                    ((Math.toDegrees(azimuthInRadians) + 360) % 360);
            Log.d("azimuth", "compassRotation: " + azimuthInDegrees);
            if (System.currentTimeMillis() - shakeRotationTime > 1000) {
                continueCompassAnimation = true;
            }
            float differenceInDegrees = currentDegree - (-azimuthInDegrees);
            if (differenceInDegrees < 300 && differenceInDegrees > -300 && continueCompassAnimation) {
                RotateAnimation rotateAnimation = new RotateAnimation(
                        currentDegree, -azimuthInDegrees,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setDuration(200);
                rotateAnimation.setFillAfter(true);
                ivCompass.startAnimation(rotateAnimation);
            }
            currentDegree = -azimuthInDegrees;
            lastTimeUpdateCompass = System.currentTimeMillis();

        }
    }

    public void shakeRotation(SensorEvent sensorEvent) {
        if (sensorEvent.sensor == accelerometer) {
            // significant motion
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            if (isFirstValue) {
                float deltaX = Math.abs(last_x - x);
                float deltaY = Math.abs(last_y - y);
                float deltaZ = Math.abs(last_z - z);
                if ((deltaX > shakeThreshold && deltaY > shakeThreshold) ||
                        (deltaX > shakeThreshold && deltaZ > shakeThreshold) ||
                        (deltaY > shakeThreshold && deltaZ > shakeThreshold)) {
                    v.vibrate(250);
                    RotateAnimation rotateAnimation = new RotateAnimation(
                            currentDegree, currentDegree + random.nextInt(2160) - 1080,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    rotateAnimation.setDuration(1000);
                    rotateAnimation.setFillAfter(true);
                    ivCompass.startAnimation(rotateAnimation);
                    shakeRotationTime = System.currentTimeMillis();
                    continueCompassAnimation = false;
                }
            }
            last_x = x;
            last_y = y;
            last_z = z;
            isFirstValue = true;
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    private class MyServiceConnection implements ServiceConnection {

        private MainActivity mainActivity;

        public MyServiceConnection(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            StepsService.LocalBinder binder = (StepsService.LocalBinder) service;
            mainActivity.service = binder.getService();
            mainActivity.service.setUsername(mainActivity.username);
            mainActivity.service.setPassword(mainActivity.password);
            mainActivity.bound = true;
            mainActivity.service.setListenerActivity(mainActivity);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mainActivity.bound = false;
        }
    }
}
