package com.lfo.p1sensors;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class InfoActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor sensor;
    private TextView tvSensorInfo;
    private Button btnStartSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        Bundle extras = getIntent().getExtras();
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (int i = 0; i <= sensorList.size(); i++) {
            if (i == extras.getInt("sensor")) {
                sensor = sensorList.get(i);
            }
        }

        tvSensorInfo = findViewById(R.id.tvSensorInfo);
        tvSensorInfo.setText(stringProcessor(sensor.toString()));

        btnStartSensor = findViewById(R.id.btnStartSensor);
        btnStartSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InfoActivity.this, SensorActivity.class);
                intent.putExtra("sensor", sensor.getType());
                InfoActivity.this.startActivity(intent);
                Log.d("Sensor ", "" + sensor.getType());
            }
        });
    }

    private String stringProcessor(String str) {
        String strSensor = str;
        strSensor = strSensor.replace("{", "");
        strSensor = strSensor.replace("}", "");
        strSensor = strSensor.replace(",", "\n");
        strSensor = strSensor.replace("=", " : ");
        return strSensor;
    }
}
