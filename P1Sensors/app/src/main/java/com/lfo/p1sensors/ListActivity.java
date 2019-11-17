package com.lfo.p1sensors;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private ListView lvSensors;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        sensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
        lvSensors = findViewById(R.id.lvSensors);
        populateList();
        clickListHandler();
    }

    private void clickListHandler() {
        lvSensors.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Intent intent = new Intent(ListActivity.this, InfoActivity.class);
                intent.putExtra("sensor",pos);
                ListActivity.this.startActivity(intent);
            }
        });
    }

    private void populateList() {

        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);
        List<String> stringList = new ArrayList<String>();
        for(Sensor sensor : sensorList) {
            stringList.add(sensor.getName());
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, stringList);
        lvSensors.setAdapter(arrayAdapter);
    }
}
