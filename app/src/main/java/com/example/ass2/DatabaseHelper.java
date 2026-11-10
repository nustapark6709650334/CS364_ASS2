
package com.example.ass2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "activity_history.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_SESSIONS = "sessions";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable = "CREATE TABLE " + TABLE_SESSIONS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "steps INTEGER NOT NULL, " +
                "shakes INTEGER NOT NULL, " +
                "duration INTEGER NOT NULL, " +
                "created_at INTEGER NOT NULL" +
                ")";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        // Database migration will be handled here.
    }

    // Save a completed session
    public boolean saveSession(
            int steps,
            int shakes,
            long duration) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("steps", steps);
        values.put("shakes", shakes);
        values.put("duration", duration);
        values.put("created_at", System.currentTimeMillis());

        long result = db.insert(TABLE_SESSIONS, null, values);

        return result != -1;
    }

    // Get total steps from all sessions
    public int getTotalSteps() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(steps), 0) FROM " + TABLE_SESSIONS,
                null
        );

        int total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }

        cursor.close();

        return total;
    }

    // Get total shakes from all sessions
    public int getTotalShakes() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(SUM(shakes), 0) FROM " + TABLE_SESSIONS,
                null
        );

        int total = 0;

        if (cursor.moveToFirst()) {
            total = cursor.getInt(0);
        }

        cursor.close();

        return total;
    }

    // Get highest step record
    public int getHighestSteps() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(MAX(steps), 0) FROM " + TABLE_SESSIONS,
                null
        );

        int highest = 0;

        if (cursor.moveToFirst()) {
            highest = cursor.getInt(0);
        }

        cursor.close();

        return highest;
    }

    // Get highest shake record
    public int getHighestShakes() {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT COALESCE(MAX(shakes), 0) FROM " + TABLE_SESSIONS,
                null
        );

        int highest = 0;

        if (cursor.moveToFirst()) {
            highest = cursor.getInt(0);
        }

        cursor.close();

        return highest;
    }
}

