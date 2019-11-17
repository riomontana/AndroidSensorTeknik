package com.lfo.partynonsense.activitys;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.lfo.partynonsense.HighscoreDatabase;
import com.lfo.partynonsense.HighscoreModel;
import com.lfo.partynonsense.R;

import java.util.ArrayList;

/**
 * GUI for testing Database
 * Implemented methods can be used later
 * @author Linus Forsberg
 */
public class DummyDataBaseActivity extends AppCompatActivity {

    private EditText inputPlayer;
    private HighscoreDatabase highscoreDatabase;
    private int playerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy_db);
        highscoreDatabase = new HighscoreDatabase(this);
        inputPlayer = findViewById(R.id.inputPlayer);
    }

    /**
     * Button handler
     * adds player to database and gets the player ID from the database
     * @param view
     */
    public void addPlayerToDB(View view) {
        String playerName = inputPlayer.getText().toString();
        highscoreDatabase.addPlayer(playerName);
        playerID = highscoreDatabase.getPlayerID(playerName);
        Log.d("playerid", String.valueOf(playerID));
    }

    /**
     * button handler
     * adds score to database
     * @param view
     */
    public void addScoreToDB(View view) {
        int score = 1000;
        highscoreDatabase.addScore(playerID, score);
    }

    /**
     * button handler
     * resets database
     * @param view
     */
    public void resetDB(View view) {
        highscoreDatabase.resetDataBase();
    }

    /**
     * gets highscore list from database and prints it to the log window
     * @param view
     */
    public void getHighscoreListFromDB(View view) {
        ArrayList<HighscoreModel> highscoreList = highscoreDatabase.readHighscoreEntries();
        for(HighscoreModel entries : highscoreList)
            Log.d(entries.player, String.valueOf(entries.score));
    }
}
