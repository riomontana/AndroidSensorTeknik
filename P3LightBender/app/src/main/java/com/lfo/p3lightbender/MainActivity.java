package com.lfo.p3lightbender;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

// TODO 1. fixa 5 presets för ljusstyrka

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensorProximity;
    private Sensor sensorLight;
    private boolean hasProximitySensor;
    private boolean hasLightSensor;
    private CameraManager cameraManager;
    private String cameraID;
    private CameraCharacteristics cameraParameters;
    private float distanceFromPhone;
    private float light;
    private boolean flashlightActivated = false;
    private ContentResolver contentResolver;
    private Window window;
    private RadioButton rbScreenBrightness, rbSystemBrightness;
    private TextView tvFlashlightInfo;
    private TextView tvLightValue;
    private TextView tvBrightnessValue;
    private TextView tvPleaseChoose;
    private SeekBar adjustBrightnessBar;
    private int brightnessPreset;
    private boolean isScreenBrightnessChosen = false;
    private boolean isSystemBrightnessChosen = false;


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        checkSensors();

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraID = cameraManager.getCameraIdList()[0];
            cameraParameters = cameraManager.getCameraCharacteristics(cameraID);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        initScreenBrightness();
        initComponents();

    }

    private void initComponents() {
        rbScreenBrightness = findViewById(R.id.rbScreenBrightness);
        rbSystemBrightness = findViewById(R.id.rbSystemBrightness);
        tvFlashlightInfo = findViewById(R.id.tvFlashlightInfo);
        tvLightValue = findViewById(R.id.tvLightValue);
        tvBrightnessValue = findViewById(R.id.tvBrightnessValue);
        tvPleaseChoose = findViewById(R.id.tvPleaseChoose);
        adjustBrightnessBar = findViewById(R.id.adjustBrightnessBar);
        adjustBrightnessBar.setEnabled(false);
        adjustBrightnessBarListener();
    }

    /**
     * Sets brightness preset between 1-5 according to seek bar
     * Adding event listener and handling events for seek bar
     */
    private void adjustBrightnessBarListener() {
        adjustBrightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == 0) {
                    brightnessPreset = 1;
                }
                if (i == 1) {
                    brightnessPreset = 2;
                }
                if (i == 2) {
                    brightnessPreset = 3;
                }
                if (i == 3) {
                    brightnessPreset = 4;
                }
                if (i == 4) {
                    brightnessPreset = 5;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * Check if the device has the sensors needed for the application to work properly
     * If not, notify user
     */
    private void checkSensors() {
        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            sensorProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            hasProximitySensor = true;
        } else {
            Toast.makeText(MainActivity.this,
                    "Proximity sensor is not installed on this device",
                    Toast.LENGTH_SHORT).show();
            hasProximitySensor = false;
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            sensorLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            hasLightSensor = true;
        } else {
            Toast.makeText(MainActivity.this,
                    "Light sensor is not installed on this device",
                    Toast.LENGTH_SHORT).show();
            hasLightSensor = false;
        }
    }

    /**
     * Register sensor listeners
     */
    protected void onResume() {
        super.onResume();
        // Adds sensor listeners if the device has the sensors installed
        if (hasLightSensor && hasProximitySensor) {
            sensorManager.registerListener(
                    this, sensorProximity, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(
                    this, sensorLight, SensorManager.SENSOR_DELAY_NORMAL);
            Toast.makeText(MainActivity.this,
                    "Sensor listeners registered", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Unregister sensor listeners
     */
    protected void onPause() {
        super.onPause();
        // Unregisters sensor listeners if the device has the sensors installed
        if (hasLightSensor && hasProximitySensor) {
            sensorManager.unregisterListener(this);
            Toast.makeText(MainActivity.this,
                    "Sensor listeners unregistered", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Set sensor manager and sensors to null
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // set sensorManager and sensors to null for garbage collection
        sensorManager = null;
        sensorProximity = null;
        sensorLight = null;
    }


    /**
     * Activate flashlight
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void activateFlashlight() {
        if (cameraParameters.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
            try {
                cameraManager.setTorchMode(cameraID, true);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            flashlightActivated = true;
        }
    }

    /**
     * Deactivate flashlight
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void deactivateFlashlight() {
        if (cameraParameters.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
            try {
                cameraManager.setTorchMode(cameraID, false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            flashlightActivated = false;
        }
    }

    /**
     * Get control of the system settings and the current window
     * The ContentResolver provides a handle to the system settings.
     * The Window object provides the access to the current visible window.
     */
    public void initScreenBrightness() {
        contentResolver = getContentResolver();
        window = getWindow();
    }

    /**
     * Change screen brightness.
     *
     * @param brightness brightness value of screen
     */
    private void changeScreenBrightness(float brightness) {

        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = brightness;
        window.setAttributes(layoutParams);
        tvBrightnessValue.setText(String.valueOf(Math.round(brightness * 100)) + " %");

    }

    /**
     * Change system brightness.
     *
     * @param brightness brightness value of screen
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void changeSystemBrightness(float brightness) {
        // check if the Activity (this) has permission to write settings.
        // if not, request permission by launching an implicit intent.
        if (!Settings.System.canWrite(this)) {
            // open androids write permission menu.
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            // The user must manually scroll down the app list and give permission the app.
            startActivity(intent);
            Log.d("Can not write:", "");
            // Changing brightness is now permitted for future use
            // unless the app is uninstalled or the user manually denies the permission.
        } else {
            Log.d("Can write:", String.valueOf(Math.round(brightness * 255)));
            Settings.System.putInt(contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS, Math.round(brightness * 255));
            tvBrightnessValue.setText(String.valueOf(Math.round(brightness * 100)) + " %");
        }
    }


    /**
     * Handling radio button clicks
     * Gives user information and updates GUI
     *
     * @param view
     */
    public void onRadioButtonClick(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.rbScreenBrightness:
                if (checked) {
                    isSystemBrightnessChosen = false;
                    isScreenBrightnessChosen = true;
                    Toast.makeText(
                            MainActivity.this, "Screen brightness chosen",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rbSystemBrightness:
                if (checked) {
                    isScreenBrightnessChosen = false;
                    isSystemBrightnessChosen = true;
                    Toast.makeText(
                            MainActivity.this,
                            "Changing system brightness is permanent",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
        adjustBrightnessBar.setEnabled(true);
        tvPleaseChoose.setText("");
    }

    /**
     * Handling sensor events.
     * Listening for changes in proximity and light sensors
     *
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            // Uses readings from proximity sensor to decide if flashlight should be activated or deactivated
            case Sensor.TYPE_PROXIMITY:
                distanceFromPhone = sensorEvent.values[0];
                if (distanceFromPhone < sensorProximity.getMaximumRange()) {
                    if (!flashlightActivated && light < 20) {
                        activateFlashlight();
                    }
                } else {
                    if (flashlightActivated) {
                        deactivateFlashlight();
                    }
                }
                break;

            // Uses readings from light sensor to change brightness of the screen
            // Also notifies user if flashlight can be activated.
            case Sensor.TYPE_LIGHT:
                light = sensorEvent.values[0];
                tvLightValue.setText(String.valueOf(Math.round(light)));

                if (light > 0) {
                    float brightness = ((1 - (1 / (light / 5))) * brightnessPreset) / 5;
                    if (isScreenBrightnessChosen) {
                        changeScreenBrightness(brightness);
                    }
                    if (isSystemBrightnessChosen) {
                        changeSystemBrightness(brightness);
                    }
                }
                if (light < 20) {
                    tvFlashlightInfo.setText(
                            "Flashlight can be activated " +
                                    "\nby covering the proximity sensor");
                } else {
                    tvFlashlightInfo.setText("");
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }


}
