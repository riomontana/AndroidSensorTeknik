package com.lfo.partynonsense.activitys;


import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.lfo.partynonsense.HighscoreDatabase;
import com.lfo.partynonsense.HighscoreModel;
import com.lfo.partynonsense.R;

import java.util.ArrayList;

/**
 * Edited by Linus Forsberg
 * Added permission checks
 */


public class MainActivity extends AppCompatActivity {

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private int nbrPlayers = 1;
    private int time = 20;
    private String[] playerNames;
    TextView currentPlayers;
    private HighscoreDatabase highscoreDB;
    String[] items = new String[]{"One player", "Two players", "Three players", "Four players"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check for permissions for microphone, sensors etc..
        // TODO add more permission-checks
        // If permission is not granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.VIBRATE},
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);


            // Permission OK!
        }

        setContentView(R.layout.activity_main);

        highscoreDB = new HighscoreDatabase(this);
        currentPlayers = findViewById(R.id.currentPlayers);
        currentPlayers.setText("Current players: " + nbrPlayers);

        Button playBtn = (Button) findViewById(R.id.playBtn);
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();

            }
        });

        Button optionBtn = (Button) findViewById(R.id.optionBtn);
        optionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optMenu();
            }
        });
        Button highscoreBtn = (Button) findViewById(R.id.highscoreBtn);
        highscoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHighscore();

            }
        });


    }
    private  void play(){
        final AlertDialog.Builder playerBuilder = new AlertDialog.Builder(MainActivity.this);
        final View playerView = getLayoutInflater().inflate(R.layout.player_name_layout, null);
        playerBuilder.setTitle("Player Names");
        playerBuilder.setView(playerView);
        final EditText player1 = playerView.findViewById(R.id.player1);
        final EditText player2 = playerView.findViewById(R.id.player2);
        final EditText player3 = playerView.findViewById(R.id.player3);
        final EditText player4 = playerView.findViewById(R.id.player4);
        if(nbrPlayers == 1){
            player2.setVisibility(View.GONE);
            player3.setVisibility(View.GONE);
            player4.setVisibility(View.GONE);
        }else if(nbrPlayers == 2){
            player3.setVisibility(View.GONE);
            player4.setVisibility(View.GONE);
        }else if(nbrPlayers == 3){
            player4.setVisibility(View.GONE);
        }else{
        }
        final AlertDialog dialog = playerBuilder.create();
        dialog.show();
        Button start = playerView.findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playerNames = new String[nbrPlayers];
                if(nbrPlayers == 1){
                    playerNames[0] = player1.getText().toString();
                }else if(nbrPlayers == 2){
                    playerNames[0] = player1.getText().toString();
                    playerNames[1] = player2.getText().toString();
                }else if(nbrPlayers == 3){
                    playerNames[0] = player1.getText().toString();
                    playerNames[1] = player2.getText().toString();
                    playerNames[2] = player3.getText().toString();
                }else{
                    playerNames[0] = player1.getText().toString();
                    playerNames[1] = player2.getText().toString();
                    playerNames[2] = player3.getText().toString();
                    playerNames[3] = player4.getText().toString();
                }
                for(int i = 0; i < playerNames.length; i++){
                    if(playerNames[i].equals("")){
                        playerNames[i] = "Player " + (i+1);
                    }
                }
                dialog.dismiss();
                change();
            }
        });
    }

    private void change() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("nbrOfPlayers", nbrPlayers);
        intent.putExtra("time", time);
        intent.putExtra("playerNames", playerNames);
        startActivity(intent);
    }

    private void optMenu(){
        final AlertDialog.Builder optBuilder = new AlertDialog.Builder(MainActivity.this);
        final View optView = getLayoutInflater().inflate(R.layout.options_layout, null);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        optBuilder.setTitle("Options");
        final Spinner dropdown = optView.findViewById(R.id.playersSpinn);
        dropdown.setAdapter(adapter);
        optBuilder.setView(optView);
        final AlertDialog dialog = optBuilder.create();
        dialog.show();
        final EditText timeET = optView.findViewById(R.id.timeET);
        Button saveBtn = optView.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(dropdown.getSelectedItem().toString().equals("One player")){
                    nbrPlayers = 1;
                }else if(dropdown.getSelectedItem().toString().equals("Two players")){
                    nbrPlayers = 2;
                }else if(dropdown.getSelectedItem().toString().equals("Three players")){
                    nbrPlayers = 3;
                }else{
                    nbrPlayers = 4;
                }
                currentPlayers.setText("Current players: " + nbrPlayers);
                if(!timeET.getText().toString().equals("")) {
                    int timeNbr = Integer.parseInt(timeET.getText().toString());
                    if (timeNbr < 91 && timeNbr > 19) {
                        time = timeNbr;
                    } else {
                        time = 20;
                    }
                }
                dialog.dismiss();

            }
        });
    }
    private void showHighscore(){
        final AlertDialog.Builder highscoreBuilder = new AlertDialog.Builder(MainActivity.this);
        final View highscoreView = getLayoutInflater().inflate(R.layout.highscore_layout, null);
        highscoreBuilder.setTitle("Highscore");
        highscoreBuilder.setView(highscoreView);
        final AlertDialog dialog = highscoreBuilder.create();
        TextView[] highscoreList = new TextView[10];
        highscoreList[0] = highscoreView.findViewById(R.id.first);
        highscoreList[1] = highscoreView.findViewById(R.id.second);
        highscoreList[2] = highscoreView.findViewById(R.id.third);
        highscoreList[3] = highscoreView.findViewById(R.id.fourth);
        highscoreList[4] = highscoreView.findViewById(R.id.fifth);
        highscoreList[5] = highscoreView.findViewById(R.id.sixth);
        highscoreList[6] = highscoreView.findViewById(R.id.seventh);
        highscoreList[7] = highscoreView.findViewById(R.id.eighth);
        highscoreList[8] = highscoreView.findViewById(R.id.ninth);
        highscoreList[9] = highscoreView.findViewById(R.id.tenth);
        dialog.show();
        ArrayList<HighscoreModel> highscoreListDB = highscoreDB.readHighscoreEntries();
        int hsNav = 0;
        for(HighscoreModel entries : highscoreListDB) {
            highscoreList[hsNav].setText((hsNav+1) + ". " + entries.player + " " + entries.score + " points" );
            hsNav++;
        }
        dialog.show();
    }

    @Override
    public void onBackPressed() {

    }
}
