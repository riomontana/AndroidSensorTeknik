package com.lfo.p4pathfinder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by LFO on 2018-03-02.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "StepDatabase";
    private static final String TABLE_STEPS_SUM = "StepsSum";
    private static final String ID = "id";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String STEPS_COUNT = "steps_count";
    private static final String CREATION_DATE = "creation_date";
    private static final String CREATE_TABLE_STEPS_SUM = "CREATE TABLE "
            + TABLE_STEPS_SUM + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + USERNAME + " TEXT, " + PASSWORD + " TEXT, "
            + CREATION_DATE + " TEXT, " + STEPS_COUNT + " INTEGER)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("", CREATE_TABLE_STEPS_SUM);
        db.execSQL(CREATE_TABLE_STEPS_SUM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void deleteUserStepHistory(String username) {
        String selectQuery = "DELETE FROM " + TABLE_STEPS_SUM +
                " WHERE " + USERNAME + " = '" + username + "' AND " + STEPS_COUNT + " > 0";
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(selectQuery);
        Log.d("", "user step history deleted");
    }

    public void registerUser(String username, String password) {
//        String selectQuery = "INSERT INTO " + TABLE_STEPS_SUM +
//                " VALUES " + "(null, " + username + ", " + password + ", null, null)";
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(PASSWORD, password);
        db.insert(TABLE_STEPS_SUM, null, values);
        Log.d("", "registerUser: user successfully saved in db");
        db.close();
    }

    public boolean checkUsernamePassword(String callingMethod, String inUsername, String inPassword) {
        boolean usernameAndPasswordExist = false;
        String selectQuery = "SELECT * FROM " + TABLE_STEPS_SUM + " WHERE " + USERNAME + " = '" + inUsername +
                "' AND " + PASSWORD + " = '" + inPassword + "'";
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    String dbUsername = c.getString(c.getColumnIndex(USERNAME));
                    String dbPassword = c.getString(c.getColumnIndex(PASSWORD));
                    if (callingMethod == "login") {
                        if ((dbUsername.equals(inUsername)) &&
                                (dbPassword.equals(inPassword))) {
                            usernameAndPasswordExist = true;
                        }
                    } else if (callingMethod == "newUser") {
                        if (dbUsername.equals(inUsername)) {
                            usernameAndPasswordExist = true;
                        }
                    }
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("USERNAME AND PASSWORD", String.valueOf(usernameAndPasswordExist));
        return usernameAndPasswordExist;
    }

    public boolean createStepsEntry(String username, String password) {
        boolean isDateAlreadyPresent = false;
        boolean createSuccessful = false;
        int currentDateStepCounts = 0;
        Calendar calendar = Calendar.getInstance();
        String todayDate =
                String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + "/" +
                        String.valueOf(calendar.get(Calendar.MONTH) + 1) + "/" +
                        String.valueOf(calendar.get(Calendar.YEAR));
        String selectQuery = "SELECT " + STEPS_COUNT + " FROM " +
                TABLE_STEPS_SUM + " WHERE " + CREATION_DATE + " = '" + todayDate +
                "' AND USERNAME = '" + username + "'";

        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    isDateAlreadyPresent = true;
                    currentDateStepCounts = c.getInt(c.getColumnIndex(STEPS_COUNT));
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CREATION_DATE, todayDate);
            values.put(USERNAME, username);
            values.put(PASSWORD, password);
            if (isDateAlreadyPresent) {
                values.put(STEPS_COUNT, ++currentDateStepCounts);
                int row = db.update(TABLE_STEPS_SUM, values,
                        CREATION_DATE + " = '" + todayDate +
                                "' AND USERNAME = '" + username + "'", null);
                if (row == 1) {
                    createSuccessful = true;
                }
                db.close();
            } else {
                values.put(STEPS_COUNT, 1);
                long row = db.insert(TABLE_STEPS_SUM, null, values);
                if (row != -1) {
                    createSuccessful = true;
                }
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Database", "Steps entry CREATED");
        return createSuccessful;
    }

    public ArrayList<DateStepsModel> readStepsEntries(String username) {
        ArrayList<DateStepsModel> stepCountList = new ArrayList<DateStepsModel>();
        String selectQuery = "SELECT * FROM " + TABLE_STEPS_SUM +
                " WHERE " + USERNAME + " = '" + username + "'";
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    DateStepsModel dateStepsModel = new DateStepsModel();
                    dateStepsModel.date = c.getString(c.getColumnIndex(CREATION_DATE));
                    dateStepsModel.stepCount = c.getInt(c.getColumnIndex(STEPS_COUNT));
                    stepCountList.add(dateStepsModel);
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Database", "Steps list returned");
        return stepCountList;
    }

    // todo ta bort sen
    public void readAll() {
        String selectQuery = "SELECT * FROM " + TABLE_STEPS_SUM;
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor c = db.rawQuery(selectQuery, null);
            if (c.moveToFirst()) {
                do {
                    DateStepsModel dateStepsModel = new DateStepsModel();
                    dateStepsModel.username = c.getString(c.getColumnIndex(USERNAME));
                    dateStepsModel.password = c.getString(c.getColumnIndex(PASSWORD));
                    dateStepsModel.date = c.getString(c.getColumnIndex(CREATION_DATE));
                    dateStepsModel.stepCount = c.getInt(c.getColumnIndex(STEPS_COUNT));
                    Log.d("user", dateStepsModel.username + "\n");
                    Log.d("passw", dateStepsModel.password + "\n");
                    Log.d("date", dateStepsModel.date + "\n");
                    Log.d("steps", String.valueOf(dateStepsModel.stepCount) + "\n");
                } while (c.moveToNext());
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
