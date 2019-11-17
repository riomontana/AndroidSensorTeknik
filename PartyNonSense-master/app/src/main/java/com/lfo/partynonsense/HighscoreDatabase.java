package com.lfo.partynonsense;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;

/**
 * Creating database for highscores and handles queries for writing and reading
 *
 * @author Linus Forsberg
 */

public class HighscoreDatabase extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "HighscoreDatabase";
    private static final String TABLE_HIGHSCORES = "Highscores";
    private static final String ID = "id";
    private static final String PLAYER = "player";
    private static final String SCORE = "score";
    private static final String CREATE_TABLE_HIGHSCORES = "CREATE TABLE "
            + TABLE_HIGHSCORES + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PLAYER + " TEXT, " + SCORE + " INTEGER)";
    private static final String RESET_DB = "DROP TABLE " + TABLE_HIGHSCORES;

    public HighscoreDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("onCreate", CREATE_TABLE_HIGHSCORES);
        db.execSQL(CREATE_TABLE_HIGHSCORES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

    }

    /**
     * reset database and creates a new empty table
     */
    public void resetDataBase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(RESET_DB);
        Log.d("resetDataBase", "reset ok");
        db.execSQL(CREATE_TABLE_HIGHSCORES);
        Log.d("resetDataBase", "table created");
    }

    /**
     * adds a player to the database
     * @param playerName
     */
    public void addPlayer(String playerName) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(PLAYER, playerName);
            values.put(SCORE, 0);
            db.insert(TABLE_HIGHSCORES, null, values);
            Log.d("addPlayer", playerName);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * gets the player id from the database
     * @param playerName name of the player
     * @return player id
     */
    public int getPlayerID(String playerName) {
        int playerID = 0;
        String selectQuery = "SELECT ID FROM " + TABLE_HIGHSCORES
                + " WHERE player = '" + playerName + "'";
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    playerID = c.getInt(c.getColumnIndex(ID));
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playerID;
    }

    /**
     * add score to a players current score
     * @param playerID id of current player
     * @param addScore the score that will be added
     */
    public void addScore(int playerID, int addScore) {
        int currentScore = 0;
        String selectQuery = "SELECT " + SCORE + " FROM " +
                TABLE_HIGHSCORES + " WHERE " + ID + " = '" + playerID + "'";
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    currentScore = c.getInt(c.getColumnIndex(SCORE));
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            int updatedScore = currentScore + addScore;
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(SCORE, updatedScore);
            db.update(TABLE_HIGHSCORES, values, ID +
                    " = '" + playerID + "'", null);
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * gets the current highscore list from the database and returns it to the calling method
     * @return highscore list in descending order
     */
    public ArrayList<HighscoreModel> readHighscoreEntries() {
        ArrayList<HighscoreModel> highscoreList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_HIGHSCORES + " ORDER BY SCORE DESC LIMIT 10";
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    HighscoreModel highscoreModel = new HighscoreModel();
                    highscoreModel.player = c.getString(c.getColumnIndex(PLAYER));
                    highscoreModel.score = c.getInt(c.getColumnIndex(SCORE));
                    highscoreModel.id = c.getInt(c.getColumnIndex(ID));
                    highscoreList.add(highscoreModel);
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("readHighscoreEntries", "list returned");
        return highscoreList;
    }
}
