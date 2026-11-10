package com.example.ass2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DBHelper: คลาสดำเนินการจัดการฐานข้อมูล SQLite สำหรับบันทึก ดึงข้อมูล และลบประวัติกิจกรรม
 * รองรับเกณฑ์การตรวจ CS364 Assignment 2 Checklist ข้อ 6 ถึง 8:
 * - ข้อ 6 Insert Session: บันทึกข้อมูลกิจกรรมแต่ละรอบ (วันที่/เวลา, ก้าว, เขย่า, ระยะเวลา, ค่าเฉลี่ย, สถานะ New Record)
 * - ข้อ 7 Query & Persistence: ดึงข้อมูลประวัติกิจกรรมทั้งหมด และคงอยู่แม้ปิด-เปิดแอปใหม่
 * - ข้อ 8 Delete & Recalculate: ลบรายการกิจกรรม และคำนวณยอดสะสม/สถิติสูงสุดใหม่ถูกต้อง
 */
public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "activity_tracker.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_ACTIVITIES = "activities";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_STEPS = "steps";
    public static final String COLUMN_SHAKES = "shakes";
    public static final String COLUMN_DURATION_SECONDS = "duration_seconds";
    public static final String COLUMN_DURATION_FORMATTED = "duration_formatted";
    public static final String COLUMN_STEP_PACE = "step_pace";
    public static final String COLUMN_SHAKE_PACE = "shake_pace";
    public static final String COLUMN_IS_NEW_STEP_RECORD = "is_new_step_record";
    public static final String COLUMN_IS_NEW_SHAKE_RECORD = "is_new_shake_record";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_ACTIVITIES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TIMESTAMP + " TEXT NOT NULL, " +
            COLUMN_STEPS + " INTEGER NOT NULL DEFAULT 0, " +
            COLUMN_SHAKES + " INTEGER NOT NULL DEFAULT 0, " +
            COLUMN_DURATION_SECONDS + " INTEGER NOT NULL DEFAULT 0, " +
            COLUMN_DURATION_FORMATTED + " TEXT NOT NULL DEFAULT '00:00', " +
            COLUMN_STEP_PACE + " REAL NOT NULL DEFAULT 0.0, " +
            COLUMN_SHAKE_PACE + " REAL NOT NULL DEFAULT 0.0, " +
            COLUMN_IS_NEW_STEP_RECORD + " INTEGER NOT NULL DEFAULT 0, " +
            COLUMN_IS_NEW_SHAKE_RECORD + " INTEGER NOT NULL DEFAULT 0" +
            ");";

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
        onCreate(db);
    }

    // =========================================================================
    // ข้อ 6: Insert Session (เพิ่มรอบกิจกรรมลง SQLite พร้อมสถานะ New Record)
    // =========================================================================

    /**
     * บันทึกข้อมูลกิจกรรม 1 รอบลง SQLite
     *
     * @param record อ็อบเจกต์ข้อมูลกิจกรรม
     * @return id ของแถวที่เพิ่มสำเร็จ (-1 หากผิดพลาด)
     */
    public long insertActivity(ActivityRecord record) {
        if (record == null) {
            return -1;
        }

        if (record.getTimestamp() == null || record.getTimestamp().trim().isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            record.setTimestamp(sdf.format(new Date()));
        }

        if (record.getDurationFormatted() == null || record.getDurationFormatted().trim().isEmpty()) {
            int sec = record.getDurationSeconds();
            int minutes = sec / 60;
            int remainingSec = sec % 60;
            record.setDurationFormatted(String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSec));
        }

        SQLiteDatabase db = this.getWritableDatabase();

        // ตรวจสอบว่ารอบนี้ทำลายสถิติสูงสุดเดิมหรือไม่
        int prevMaxSteps = getMaxSteps();
        int prevMaxShakes = getMaxShakes();

        boolean isNewStep = record.getSteps() > prevMaxSteps && record.getSteps() > 0;
        boolean isNewShake = record.getShakes() > prevMaxShakes && record.getShakes() > 0;

        record.setNewStepRecord(isNewStep);
        record.setNewShakeRecord(isNewShake);

        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, record.getTimestamp());
        values.put(COLUMN_STEPS, record.getSteps());
        values.put(COLUMN_SHAKES, record.getShakes());
        values.put(COLUMN_DURATION_SECONDS, record.getDurationSeconds());
        values.put(COLUMN_DURATION_FORMATTED, record.getDurationFormatted());
        values.put(COLUMN_STEP_PACE, record.getStepPace());
        values.put(COLUMN_SHAKE_PACE, record.getShakePace());
        values.put(COLUMN_IS_NEW_STEP_RECORD, isNewStep ? 1 : 0);
        values.put(COLUMN_IS_NEW_SHAKE_RECORD, isNewShake ? 1 : 0);

        long id = db.insert(TABLE_ACTIVITIES, null, values);
        record.setId(id);

        // คำนวณสถานะสถิติสูงสุดใหม่เพื่อให้สอดคล้องกันทั่วทั้งระบบ
        recalculateRecords();

        return id;
    }

    /**
     * Overloaded method สำหรับเพิ่มข้อมูลกิจกรรม
     */
    public long insertActivity(String timestamp, int steps, int shakes, int durationSeconds,
                               String durationFormatted, double stepPace, double shakePace,
                               boolean isNewStepRecord, boolean isNewShakeRecord) {
        ActivityRecord record = new ActivityRecord(
                timestamp, steps, shakes, durationSeconds, durationFormatted,
                stepPace, shakePace, isNewStepRecord, isNewShakeRecord
        );
        return insertActivity(record);
    }

    // =========================================================================
    // ข้อ 7: Query & Persistence (ดึงประวัติกิจกรรมทั้งหมด เรียงจากใหม่ไปเก่า)
    // =========================================================================

    /**
     * ดึงรายการกิจกรรมทั้งหมด เรียงตาม ID จากมากไปน้อย (ล่าสุดอยู่บนสุด)
     */
    public List<ActivityRecord> getAllActivities() {
        List<ActivityRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_ACTIVITIES,
                null,
                null,
                null,
                null,
                null,
                COLUMN_ID + " DESC"
        );

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    list.add(cursorToRecord(cursor));
                }
            } finally {
                cursor.close();
            }
        }
        return list;
    }

    /**
     * ดึงรายการกิจกรรมเดี่ยวตาม ID
     */
    public ActivityRecord getActivityById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_ACTIVITIES,
                null,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        ActivityRecord record = null;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    record = cursorToRecord(cursor);
                }
            } finally {
                cursor.close();
            }
        }
        return record;
    }

    /**
     * แปลง Cursor ปัจจุบันเป็น ActivityRecord
     */
    private ActivityRecord cursorToRecord(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
        int steps = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS));
        int shakes = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SHAKES));
        int durationSec = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION_SECONDS));
        String durationFmt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DURATION_FORMATTED));
        double stepPace = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_STEP_PACE));
        double shakePace = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SHAKE_PACE));
        boolean isNewStep = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_NEW_STEP_RECORD)) == 1;
        boolean isNewShake = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_NEW_SHAKE_RECORD)) == 1;

        return new ActivityRecord(id, timestamp, steps, shakes, durationSec, durationFmt,
                stepPace, shakePace, isNewStep, isNewShake);
    }

    // =========================================================================
    // ข้อ 8: Delete & Recalculate (ลบรายการ และคำนวณยอดสะสม/สถิติสูงสุดใหม่)
    // =========================================================================

    /**
     * ลบรายการกิจกรรมตาม ID และคำนวณป้าย New Record ใหม่ทันที
     *
     * @param id รหัสรายการที่ต้องการลบ
     * @return จำนวนแถวที่ถูกลบ
     */
    public int deleteActivity(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_ACTIVITIES, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        if (rowsDeleted > 0) {
            recalculateRecords();
        }
        return rowsDeleted;
    }

    /**
     * ล้างข้อมูลประวัติกิจกรรมทั้งหมด
     */
    public int deleteAllActivities() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_ACTIVITIES, null, null);
        return rowsDeleted;
    }

    /**
     * คำนวณและอัปเดตป้ายสถิติสูงสุด (NEW STEP RECORD / NEW SHAKE RECORD) ใหม่
     * โดยค้นหาแถวที่มี steps และ shakes สูงสุดในฐานข้อมูลที่ยังเหลืออยู่
     */
    public void recalculateRecords() {
        SQLiteDatabase db = this.getWritableDatabase();

        // 1. รีเซ็ตป้ายสถิติทั้งหมดก่อน
        ContentValues resetValues = new ContentValues();
        resetValues.put(COLUMN_IS_NEW_STEP_RECORD, 0);
        resetValues.put(COLUMN_IS_NEW_SHAKE_RECORD, 0);
        db.update(TABLE_ACTIVITIES, resetValues, null, null);

        // 2. หาแถวที่มีจำนวนก้าวสูงสุด (steps > 0)
        Cursor stepCursor = db.rawQuery(
                "SELECT " + COLUMN_ID + " FROM " + TABLE_ACTIVITIES +
                " WHERE " + COLUMN_STEPS + " > 0 ORDER BY " + COLUMN_STEPS + " DESC, " + COLUMN_ID + " ASC LIMIT 1",
                null
        );
        if (stepCursor != null) {
            try {
                if (stepCursor.moveToFirst()) {
                    long stepRecordId = stepCursor.getLong(0);
                    ContentValues v = new ContentValues();
                    v.put(COLUMN_IS_NEW_STEP_RECORD, 1);
                    db.update(TABLE_ACTIVITIES, v, COLUMN_ID + " = ?", new String[]{String.valueOf(stepRecordId)});
                }
            } finally {
                stepCursor.close();
            }
        }

        // 3. หาแถวที่มีจำนวนเขย่าสูงสุด (shakes > 0)
        Cursor shakeCursor = db.rawQuery(
                "SELECT " + COLUMN_ID + " FROM " + TABLE_ACTIVITIES +
                " WHERE " + COLUMN_SHAKES + " > 0 ORDER BY " + COLUMN_SHAKES + " DESC, " + COLUMN_ID + " ASC LIMIT 1",
                null
        );
        if (shakeCursor != null) {
            try {
                if (shakeCursor.moveToFirst()) {
                    long shakeRecordId = shakeCursor.getLong(0);
                    ContentValues v = new ContentValues();
                    v.put(COLUMN_IS_NEW_SHAKE_RECORD, 1);
                    db.update(TABLE_ACTIVITIES, v, COLUMN_ID + " = ?", new String[]{String.valueOf(shakeRecordId)});
                }
            } finally {
                shakeCursor.close();
            }
        }
    }

    /**
     * คำนวณผลรวมจำนวนก้าวทั้งหมดจากประวัติ
     */
    public int getTotalSteps() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_STEPS + ") FROM " + TABLE_ACTIVITIES, null);
        int total = 0;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    total = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }
        return total;
    }

    /**
     * คำนวณผลรวมจำนวนเขย่าทั้งหมดจากประวัติ
     */
    public int getTotalShakes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_SHAKES + ") FROM " + TABLE_ACTIVITIES, null);
        int total = 0;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    total = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }
        return total;
    }

    /**
     * หาสถิติก้าวสูงสุดในประวัติ
     */
    public int getMaxSteps() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + COLUMN_STEPS + ") FROM " + TABLE_ACTIVITIES, null);
        int max = 0;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    max = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }
        return max;
    }

    /**
     * หาสถิติเขย่าสูงสุดในประวัติ
     */
    public int getMaxShakes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(" + COLUMN_SHAKES + ") FROM " + TABLE_ACTIVITIES, null);
        int max = 0;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    max = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }
        return max;
    }

    /**
     * นับจำนวนรอบกิจกรรมทั้งหมดในฐานข้อมูล
     */
    public int getActivityCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ACTIVITIES, null);
        int count = 0;
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }
        return count;
    }

    /**
     * คืนค่าข้อความสรุปยอดสะสมจากประวัติ เช่น "ยอดสะสมจากประวัติ X ก้าว • เขย่า Y ครั้ง"
     */
    public String getTotalSummaryText() {
        return "ยอดสะสมจากประวัติ " + getTotalSteps() + " ก้าว • เขย่า " + getTotalShakes() + " ครั้ง";
    }

    // =========================================================================
    // Data Model: ActivityRecord สำหรับจัดเก็บและส่งต่อข้อมูลกิจกรรมแต่ละรอบ
    // =========================================================================

    public static class ActivityRecord {
        private long id;
        private String timestamp;
        private int steps;
        private int shakes;
        private int durationSeconds;
        private String durationFormatted;
        private double stepPace;
        private double shakePace;
        private boolean isNewStepRecord;
        private boolean isNewShakeRecord;

        public ActivityRecord() {
        }

        public ActivityRecord(String timestamp, int steps, int shakes, int durationSeconds,
                              String durationFormatted, double stepPace, double shakePace,
                              boolean isNewStepRecord, boolean isNewShakeRecord) {
            this.timestamp = timestamp;
            this.steps = steps;
            this.shakes = shakes;
            this.durationSeconds = durationSeconds;
            this.durationFormatted = durationFormatted;
            this.stepPace = stepPace;
            this.shakePace = shakePace;
            this.isNewStepRecord = isNewStepRecord;
            this.isNewShakeRecord = isNewShakeRecord;
        }

        public ActivityRecord(long id, String timestamp, int steps, int shakes, int durationSeconds,
                              String durationFormatted, double stepPace, double shakePace,
                              boolean isNewStepRecord, boolean isNewShakeRecord) {
            this.id = id;
            this.timestamp = timestamp;
            this.steps = steps;
            this.shakes = shakes;
            this.durationSeconds = durationSeconds;
            this.durationFormatted = durationFormatted;
            this.stepPace = stepPace;
            this.shakePace = shakePace;
            this.isNewStepRecord = isNewStepRecord;
            this.isNewShakeRecord = isNewShakeRecord;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public int getSteps() {
            return steps;
        }

        public void setSteps(int steps) {
            this.steps = steps;
        }

        public int getShakes() {
            return shakes;
        }

        public void setShakes(int shakes) {
            this.shakes = shakes;
        }

        public int getDurationSeconds() {
            return durationSeconds;
        }

        public void setDurationSeconds(int durationSeconds) {
            this.durationSeconds = durationSeconds;
        }

        public String getDurationFormatted() {
            return durationFormatted;
        }

        public void setDurationFormatted(String durationFormatted) {
            this.durationFormatted = durationFormatted;
        }

        public double getStepPace() {
            return stepPace;
        }

        public void setStepPace(double stepPace) {
            this.stepPace = stepPace;
        }

        public double getShakePace() {
            return shakePace;
        }

        public void setShakePace(double shakePace) {
            this.shakePace = shakePace;
        }

        public boolean isNewStepRecord() {
            return isNewStepRecord;
        }

        public void setNewStepRecord(boolean newStepRecord) {
            isNewStepRecord = newStepRecord;
        }

        public boolean isNewShakeRecord() {
            return isNewShakeRecord;
        }

        public void setNewShakeRecord(boolean newShakeRecord) {
            isNewShakeRecord = newShakeRecord;
        }

        public String getSummaryText() {
            return steps + " ก้าว • เขย่า " + shakes + " ครั้ง";
        }

        public String getPaceText() {
            return String.format(Locale.getDefault(), "เฉลี่ย: %.1f steps/min • %.1f shakes/min", stepPace, shakePace);
        }

        public boolean hasAnyRecordBadge() {
            return isNewStepRecord || isNewShakeRecord;
        }
    }
}
