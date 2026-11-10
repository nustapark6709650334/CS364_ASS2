package com.example.ass2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity: จัดการหน้าจอหลัก, เซนเซอร์ (Issue #2), ฐานข้อมูล SQLite (Issue #3)
 * และการเชื่อมโยงข้อมูลกับ RecyclerView (Issue #4)
 *
 * ส่วนการทำงานสำหรับ Issue #3 (SQLite 30%):
 * - ข้อ 6 Insert Session: บันทึกข้อมูลกิจกรรมแต่ละรอบลง SQLite
 * - ข้อ 7 Query & Persistence: โหลดประวัติกิจกรรมทั้งหมดเมื่อเปิดแอป และคงอยู่ถาวร
 * - ข้อ 8 Delete & Recalculate: ลบประวัติกิจกรรม และคำนวณยอดสะสม/สถิติสูงสุดใหม่
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // จัดการฐานข้อมูล SQLite (Issue #3)
    private DBHelper dbHelper;
    private final List<DBHelper.ActivityRecord> activityList = new ArrayList<>();

    // ข้อมูลเซสชันกิจกรรมปัจจุบัน (Current Session) ที่ได้จากเซนเซอร์ (Issue #2)
    private int currentSessionSteps = 0;
    private int currentSessionShakes = 0;
    private int currentSessionDurationSeconds = 0;
    private double currentSessionStepPace = 0.0;
    private double currentSessionShakePace = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // เริ่มต้นใช้งานฐานข้อมูล SQLite (Issue #3)
        initDatabase();

        // เชื่อมโยงปุ่มบันทึกกิจกรรม (หากมีใน Layout)
        setupSaveButtonListener();

        // โหลดข้อมูลประวัติกิจกรรมเดิมจาก SQLite (ข้อ 7 Query & Persistence)
        loadActivityHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // โหลดข้อมูลใหม่เพื่อให้สอดคล้องกับสถานะล่าสุดเสมอ
        loadActivityHistory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    /**
     * กำหนดค่าเริ่มต้นให้กับ DBHelper
     */
    private void initDatabase() {
        dbHelper = new DBHelper(this);
    }

    /**
     * ดึง DBHelper สำหรับเรียกใช้งานจากภายนอกหรือคลาสอื่น
     */
    public DBHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = new DBHelper(this);
        }
        return dbHelper;
    }

    /**
     * ดึงรายการประวัติกิจกรรมปัจจุบันที่โหลดขึ้นมาในหน่วยความจำ
     */
    public List<DBHelper.ActivityRecord> getActivityList() {
        return activityList;
    }

    // =========================================================================
    // ข้อ 7: Query & Persistence (ดึงประวัติกิจกรรม และคำนวณยอดสะสม)
    // =========================================================================

    /**
     * ดึงข้อมูลประวัติกิจกรรมทั้งหมดจากฐานข้อมูล SQLite และอัปเดตหน้าจอ
     */
    public void loadActivityHistory() {
        if (dbHelper == null) {
            return;
        }

        activityList.clear();
        activityList.addAll(dbHelper.getAllActivities());

        Log.d(TAG, "โหลดประวัติกิจกรรมแล้ว: " + activityList.size() + " รายการ");
        Log.d(TAG, dbHelper.getTotalSummaryText());

        // อัปเดตยอดสะสมจากประวัติบน UI (ถ้ามี TextView ที่เกี่ยวข้อง)
        updateTotalSummaryUI();
    }

    /**
     * อัปเดตข้อความยอดสะสมบน UI หากมี View ที่ตรงกับ ID
     */
    private void updateTotalSummaryUI() {
        if (dbHelper == null) return;

        int summaryId = getResources().getIdentifier("tvTotalSummary", "id", getPackageName());
        if (summaryId == 0) {
            summaryId = getResources().getIdentifier("tv_total_summary", "id", getPackageName());
        }

        if (summaryId != 0) {
            TextView tvSummary = findViewById(summaryId);
            if (tvSummary != null) {
                tvSummary.setText(dbHelper.getTotalSummaryText());
            }
        }
    }

    // =========================================================================
    // ข้อ 6: Insert Session (บันทึกกิจกรรมลง SQLite)
    // =========================================================================

    /**
     * ผูก Event ให้กับปุ่ม "บันทึกกิจกรรม" (รองรับชื่อ ID ทั้ง camelCase และ snake_case)
     */
    private void setupSaveButtonListener() {
        int saveBtnId = getResources().getIdentifier("btnSaveActivity", "id", getPackageName());
        if (saveBtnId == 0) {
            saveBtnId = getResources().getIdentifier("btn_save_activity", "id", getPackageName());
        }
        if (saveBtnId == 0) {
            saveBtnId = getResources().getIdentifier("btnSave", "id", getPackageName());
        }

        if (saveBtnId != 0) {
            View saveBtn = findViewById(saveBtnId);
            if (saveBtn != null) {
                saveBtn.setOnClickListener(this::onSaveActivityClicked);
            }
        }
    }

    /**
     * เมธอดเรียกเมื่อกดปุ่ม "บันทึกกิจกรรม" (สามารถใช้ผ่าน android:onClick="onSaveActivityClicked" ได้ด้วย)
     */
    public void onSaveActivityClicked(View view) {
        saveCurrentSession(
                currentSessionSteps,
                currentSessionShakes,
                currentSessionDurationSeconds,
                currentSessionStepPace,
                currentSessionShakePace
        );
    }

    /**
     * บันทึกข้อมูลกิจกรรมของรอบปัจจุบันลงฐานข้อมูล SQLite
     *
     * @param steps           จำนวนก้าวในรอบปัจจุบัน
     * @param shakes          จำนวนเขย่าในรอบปัจจุบัน
     * @param durationSeconds ระยะเวลาทำกิจกรรม (วินาที)
     * @param stepPace        ค่าเฉลี่ยก้าวต่อนาที
     * @param shakePace       ค่าเฉลี่ยเขย่าต่อนาที
     * @return DBHelper.ActivityRecord ที่บันทึกสำเร็จ
     */
    public DBHelper.ActivityRecord saveCurrentSession(int steps, int shakes, int durationSeconds,
                                                     double stepPace, double shakePace) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(this);
        }

        DBHelper.ActivityRecord record = new DBHelper.ActivityRecord();
        record.setSteps(steps);
        record.setShakes(shakes);
        record.setDurationSeconds(durationSeconds);
        record.setStepPace(stepPace);
        record.setShakePace(shakePace);

        long id = dbHelper.insertActivity(record);
        if (id != -1) {
            Toast.makeText(this, "บันทึกกิจกรรมเรียบร้อยแล้ว (" + steps + " ก้าว)", Toast.LENGTH_SHORT).show();
            // โหลดและอัปเดตสถิติสะสมใหม่ทันที
            loadActivityHistory();
        } else {
            Toast.makeText(this, "เกิดข้อผิดพลาดในการบันทึกข้อมูล", Toast.LENGTH_SHORT).show();
        }

        return record;
    }

    // =========================================================================
    // ข้อ 8: Delete & Recalculate (ลบประวัติ และคำนวณยอดสะสม/สถิติสูงสุดใหม่)
    // =========================================================================

    /**
     * ลบรายการกิจกรรมตาม ID และคำนวณยอดสะสม/สถิติ New Record ใหม่
     *
     * @param id รหัสรายการที่จะลบ
     * @return true หากลบสำเร็จ
     */
    public boolean deleteActivityRecord(long id) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(this);
        }

        int affected = dbHelper.deleteActivity(id);
        if (affected > 0) {
            Toast.makeText(this, "ลบประวัติกิจกรรมเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
            // โหลดประวัติใหม่พร้อมอัปเดตสถิติและ New Record ทันที
            loadActivityHistory();
            return true;
        }
        return false;
    }

    /**
     * อัปเดตข้อมูลเซสชันปัจจุบันจากระบบ Sensor (เตรียมไว้สำหรับ Issue #2 เรียกใช้)
     */
    public void setCurrentSessionData(int steps, int shakes, int durationSeconds, double stepPace, double shakePace) {
        this.currentSessionSteps = steps;
        this.currentSessionShakes = shakes;
        this.currentSessionDurationSeconds = durationSeconds;
        this.currentSessionStepPace = stepPace;
        this.currentSessionShakePace = shakePace;
    }

    public int getCurrentSessionSteps() {
        return currentSessionSteps;
    }

    public int getCurrentSessionShakes() {
        return currentSessionShakes;
    }

    public int getCurrentSessionDurationSeconds() {
        return currentSessionDurationSeconds;
    }

    public double getCurrentSessionStepPace() {
        return currentSessionStepPace;
    }

    public double getCurrentSessionShakePace() {
        return currentSessionShakePace;
    }
}
