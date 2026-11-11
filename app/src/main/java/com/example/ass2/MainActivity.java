package com.example.ass2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MainActivity: จัดการระบบตรวจจับเซนเซอร์ (Sensor 50% — Issue #2),
 * ฐานข้อมูล SQLite (SQLite 30% — Issue #3) และการเตรียมข้อมูลสำหรับ RecyclerView (20% — Issue #4)
 *
 * รายการ Checklist ที่ครอบคลุม:
 * - ข้อ 1 Step Counter Sensor: ใช้งาน Sensor.TYPE_STEP_COUNTER และขอ Permission ACTIVITY_RECOGNITION
 * - ข้อ 2 Shake Detection: ใช้ Accelerometer ตรวจจับแรงสั่นสะเทือน พร้อม Threshold และ Cooldown
 * - ข้อ 3 Session Delta: คำนวณเฉพาะจำนวนก้าวและครั้งที่เขย่าที่เกิดขึ้นใหม่ในเซสชันนั้น
 * - ข้อ 4 Duration & Average: แสดงเวลา MM:SS และคำนวณอัตราเฉลี่ยก้าว/นาที, เขย่า/นาที
 * - ข้อ 5 Total & New Record: แสดงยอดสะสมจากประวัติ และป้าย NEW STEP RECORD / NEW SHAKE RECORD
 * - ข้อ 6 Insert Session: บันทึกข้อมูลกิจกรรมลง SQLite (DBHelper)
 * - ข้อ 7 Query & Persistence: ดึงข้อมูลประวัติกิจกรรมทั้งหมด และคงอยู่ถาวร
 * - ข้อ 8 Delete & Recalculate: ลบรายการกิจกรรม และคำนวณยอดสะสมรวม/สถิติสูงสุดใหม่ถูกต้อง
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_ACTIVITY_RECOGNITION = 1001;

    // Shake detection settings (ข้อ 2)
    private static final float SHAKE_THRESHOLD = 12.0f;
    private static final long SHAKE_COOLDOWN_MS = 500;

    // Sensors
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private Sensor accelerometerSensor;

    private boolean stepSensorRegistered = false;
    private boolean accelerometerRegistered = false;

    // SQLite Database (Issue #3)
    private DBHelper dbHelper;
    private final List<DBHelper.ActivityRecord> activityList = new ArrayList<>();
    private ActivityAdapter historyAdapter;

    // UI Components
    private TextView tvSteps;
    private TextView tvShakes;
    private TextView tvDuration;
    private TextView tvStepsPerMinute;
    private TextView tvShakesPerMinute;

    private TextView tvTotalHistory;
    private TextView tvNewStepRecord;
    private TextView tvNewShakeRecord;

    private Button btnStartSession;
    private Button btnStopSession;

    // Session state (ข้อ 3)
    private boolean isSessionRunning = false;
    private int latestTotalSteps = -1;
    private int sessionStartStepCount = -1;

    private int sessionSteps = 0;
    private int sessionShakes = 0;

    private long sessionStartTime = 0;
    private long sessionDuration = 0;
    private long lastShakeTime = 0;

    private double currentStepPace = 0.0;
    private double currentShakePace = 0.0;

    // Timer (ข้อ 4)
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isSessionRunning) {
                updateDurationDisplay();
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // System bar insets
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // เชื่อมโยง UI Components
        initViews();
        initHistoryViews();

        // เริ่มต้นใช้งานฐานข้อมูล SQLite (Issue #3)
        initDatabase();

        // เริ่มต้นใช้งานเซนเซอร์ (Issue #2)
        initSensors();

        // อัปเดตยอดสะสมจากประวัติเดิม (ข้อ 5 & ข้อ 7)
        loadActivityHistory();
    }

    private void initViews() {
        tvSteps = findViewById(R.id.tvSteps);
        tvShakes = findViewById(R.id.tvShakes);
        tvDuration = findViewById(R.id.tvDuration);
        tvStepsPerMinute = findViewById(R.id.tvStepsPerMinute);
        tvShakesPerMinute = findViewById(R.id.tvShakesPerMinute);

        tvTotalHistory = findViewById(R.id.tvTotalHistory);
        tvNewStepRecord = findViewById(R.id.tvNewStepRecord);
        tvNewShakeRecord = findViewById(R.id.tvNewShakeRecord);

        btnStartSession = findViewById(R.id.btnStartSession);
        btnStopSession = findViewById(R.id.btnStopSession);

        if (btnStartSession != null) {
            btnStartSession.setOnClickListener(v -> startSession());
        }
        if (btnStopSession != null) {
            btnStopSession.setOnClickListener(v -> stopSession());
            btnStopSession.setEnabled(false);
        }
    }

    private void initDatabase() {
        dbHelper = new DBHelper(this);
    }

    private void initHistoryViews() {
        RecyclerView history = findViewById(R.id.history_list);
        historyAdapter = new ActivityAdapter(this::showActivityDetails);
        history.setLayoutManager(new LinearLayoutManager(this));
        history.setAdapter(historyAdapter);
    }

    private void showActivityDetails(DBHelper.ActivityRecord record) {
        View content = getLayoutInflater().inflate(R.layout.dialog_activity_detail, null);
        TextView detail = content.findViewById(R.id.activity_detail);
        String text = getString(R.string.history_detail, record.getTimestamp(), record.getSteps(),
                record.getShakes(), record.getDurationFormatted(), record.getStepPace(),
                record.getShakePace());
        if (record.isNewStepRecord()) text += "\n" + getString(R.string.new_step_record);
        if (record.isNewShakeRecord()) text += "\n" + getString(R.string.new_shake_record);
        detail.setText(text);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.history_detail_title)
                .setView(content)
                .setNegativeButton(R.string.close_detail, null)
                .setPositiveButton(R.string.delete_activity, (dialog, which) -> {
                    if (!deleteActivityRecord(record.getId())) {
                        Toast.makeText(this, R.string.delete_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        checkAndRequestActivityRecognitionPermission();
    }

    // =========================================================================
    // Permission Management (ข้อ 1)
    // =========================================================================

    private boolean hasActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void checkAndRequestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (!hasActivityRecognitionPermission()) {
                requestPermissions(
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        REQUEST_ACTIVITY_RECOGNITION
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ACTIVITY_RECOGNITION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                registerStepSensor();
            } else {
                if (tvSteps != null) {
                    tvSteps.setText("Activity Recognition permission denied");
                }
                Toast.makeText(this, "ต้องอนุญาต Permission เพื่อนับก้าวเดิน", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void registerStepSensor() {
        if (sensorManager == null || stepCounterSensor == null || stepSensorRegistered) {
            return;
        }

        stepSensorRegistered = sensorManager.registerListener(
                this,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_UI
        );

        if (!stepSensorRegistered && tvSteps != null) {
            tvSteps.setText("Unable to register Step Counter");
        }
    }

    private void registerAccelerometer() {
        if (sensorManager == null || accelerometerSensor == null || accelerometerRegistered) {
            return;
        }

        accelerometerRegistered = sensorManager.registerListener(
                this,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_UI
        );

        if (!accelerometerRegistered && tvShakes != null) {
            tvShakes.setText("Unable to register Accelerometer");
        }
    }

    // =========================================================================
    // Session Management (ข้อ 3, 4, 5, 6)
    // =========================================================================

    public void startSession() {
        // รีเซ็ตค่าในรอบกิจกรรมใหม่ (ข้อ 3 Session Delta)
        sessionSteps = 0;
        sessionShakes = 0;
        sessionDuration = 0;
        lastShakeTime = 0;
        currentStepPace = 0.0;
        currentShakePace = 0.0;

        // บันทึกค่าเริ่มต้นของเซนเซอร์ก้าวเป็น Baseline
        sessionStartStepCount = latestTotalSteps;

        // เริ่มจับเวลา (ข้อ 4)
        sessionStartTime = SystemClock.elapsedRealtime();
        isSessionRunning = true;

        // อัปเดต UI เริ่มต้น
        if (tvSteps != null) tvSteps.setText("Steps: 0");
        if (tvShakes != null) tvShakes.setText("Shakes: 0");
        if (tvDuration != null) tvDuration.setText("Duration: 00:00");
        if (tvStepsPerMinute != null) tvStepsPerMinute.setText("Steps/min: 0");
        if (tvShakesPerMinute != null) tvShakesPerMinute.setText("Shakes/min: 0");

        if (tvNewStepRecord != null) tvNewStepRecord.setText("");
        if (tvNewShakeRecord != null) tvNewShakeRecord.setText("");

        if (btnStartSession != null) btnStartSession.setEnabled(false);
        if (btnStopSession != null) btnStopSession.setEnabled(true);

        handler.removeCallbacks(timerRunnable);
        handler.post(timerRunnable);
    }

    public void stopSession() {
        if (!isSessionRunning) {
            return;
        }

        isSessionRunning = false;
        handler.removeCallbacks(timerRunnable);

        // คำนวณระยะเวลาทั้งหมดในเซสชัน
        sessionDuration = SystemClock.elapsedRealtime() - sessionStartTime;
        updateDurationDisplay();

        // คำนวณค่าเฉลี่ยก้าวต่อนาทีและเขย่าต่อนาที (ข้อ 4)
        double durationMinutes = sessionDuration / 60000.0;
        if (durationMinutes > 0) {
            currentStepPace = sessionSteps / durationMinutes;
            currentShakePace = sessionShakes / durationMinutes;
        } else {
            currentStepPace = 0.0;
            currentShakePace = 0.0;
        }

        if (tvStepsPerMinute != null) {
            tvStepsPerMinute.setText(String.format(Locale.getDefault(), "Steps/min: %.2f", currentStepPace));
        }
        if (tvShakesPerMinute != null) {
            tvShakesPerMinute.setText(String.format(Locale.getDefault(), "Shakes/min: %.2f", currentShakePace));
        }

        // บันทึกลง SQLite (DBHelper) และตรวจสอบสถิติใหม่ (ข้อ 5, ข้อ 6)
        saveSessionAndCheckRecords();

        if (btnStartSession != null) btnStartSession.setEnabled(true);
        if (btnStopSession != null) btnStopSession.setEnabled(false);
    }

    private void updateDurationDisplay() {
        long elapsed;
        if (isSessionRunning) {
            elapsed = SystemClock.elapsedRealtime() - sessionStartTime;
        } else {
            elapsed = sessionDuration;
        }

        long totalSeconds = elapsed / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (tvDuration != null) {
            tvDuration.setText(String.format(Locale.getDefault(), "Duration: %02d:%02d", minutes, seconds));
        }
    }

    private void saveSessionAndCheckRecords() {
        if (dbHelper == null) {
            dbHelper = new DBHelper(this);
        }

        // ตรวจสอบสถิติสูงสุดเดิมก่อนบันทึก (ข้อ 5)
        int previousHighestSteps = dbHelper.getMaxSteps();
        int previousHighestShakes = dbHelper.getMaxShakes();

        int durationSeconds = (int) (sessionDuration / 1000);
        long minutes = durationSeconds / 60;
        long seconds = durationSeconds % 60;
        String formattedDuration = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        DBHelper.ActivityRecord record = new DBHelper.ActivityRecord();
        record.setSteps(sessionSteps);
        record.setShakes(sessionShakes);
        record.setDurationSeconds(durationSeconds);
        record.setDurationFormatted(formattedDuration);
        record.setStepPace(currentStepPace);
        record.setShakePace(currentShakePace);

        long id = dbHelper.insertActivity(record);
        if (id == -1) {
            Toast.makeText(this, "เกิดข้อผิดพลาดในการบันทึกกิจกรรม", Toast.LENGTH_SHORT).show();
            return;
        }

        // แสดงป้าย NEW RECORD หากทำลายสถิติเดิม (ข้อ 5)
        boolean isNewStepRecord = sessionSteps > previousHighestSteps && sessionSteps > 0;
        boolean isNewShakeRecord = sessionShakes > previousHighestShakes && sessionShakes > 0;

        if (isNewStepRecord && tvNewStepRecord != null) {
            tvNewStepRecord.setText("NEW STEP RECORD");
        } else if (tvNewStepRecord != null) {
            tvNewStepRecord.setText("");
        }

        if (isNewShakeRecord && tvNewShakeRecord != null) {
            tvNewShakeRecord.setText("NEW SHAKE RECORD");
        } else if (tvNewShakeRecord != null) {
            tvNewShakeRecord.setText("");
        }

        // โหลดข้อมูลและอัปเดตยอดสะสมจากประวัติใหม่ (ข้อ 5 & ข้อ 7)
        loadActivityHistory();

        Toast.makeText(this, "บันทึกกิจกรรมเรียบร้อยแล้ว (" + sessionSteps + " ก้าว)", Toast.LENGTH_SHORT).show();
    }

    // =========================================================================
    // SQLite Persistence & Management (ข้อ 7 & ข้อ 8)
    // =========================================================================

    public void loadActivityHistory() {
        if (dbHelper == null) {
            dbHelper = new DBHelper(this);
        }

        List<DBHelper.ActivityRecord> records = dbHelper.getAllActivities();
        activityList.clear();
        activityList.addAll(records);

        if (historyAdapter != null) {
            historyAdapter.submitItems(activityList);
            findViewById(R.id.history_empty).setVisibility(records.isEmpty() ? View.VISIBLE : View.GONE);
            findViewById(R.id.history_list).setVisibility(records.isEmpty() ? View.GONE : View.VISIBLE);
        }

        updateTotalHistory();
    }

    public void updateTotalHistory() {
        if (dbHelper == null) {
            dbHelper = new DBHelper(this);
        }

        String summary = dbHelper.getTotalSummaryText();
        if (tvTotalHistory != null) {
            tvTotalHistory.setText(summary);
        }
    }

    public boolean deleteActivityRecord(long id) {
        if (dbHelper == null) {
            dbHelper = new DBHelper(this);
        }

        int affected = dbHelper.deleteActivity(id);
        if (affected > 0) {
            loadActivityHistory();
            Toast.makeText(this, "ลบประวัติกิจกรรมเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

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
            loadActivityHistory();
        }
        return record;
    }

    public DBHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = new DBHelper(this);
        }
        return dbHelper;
    }

    public List<DBHelper.ActivityRecord> getActivityList() {
        return activityList;
    }

    // =========================================================================
    // Sensor Event Callbacks (ข้อ 1 & ข้อ 2)
    // =========================================================================

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event == null || event.sensor == null) {
            return;
        }

        // ข้อ 1: Step Counter Sensor
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            latestTotalSteps = (int) event.values[0];

            if (isSessionRunning) {
                // กำหนด Baseline เมื่อเพิ่งเริ่มกิจกรรมหรือได้รับค่าครั้งแรก
                if (sessionStartStepCount == -1) {
                    sessionStartStepCount = latestTotalSteps;
                }

                // ข้อ 3: Session Delta (นับเฉพาะก้าวใหม่ในรอบนี้)
                sessionSteps = Math.max(0, latestTotalSteps - sessionStartStepCount);

                if (tvSteps != null) {
                    tvSteps.setText("Steps: " + sessionSteps);
                }
            }
        }
        // ข้อ 2: Shake Detection (Accelerometer)
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (!isSessionRunning) {
                return;
            }

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // คำนวณ Magnitude
            double magnitude = Math.sqrt(x * x + y * y + z * z);

            // หักลบค่าแรงโน้มถ่วงของโลก
            double dynamicAcceleration = Math.abs(magnitude - SensorManager.GRAVITY_EARTH);

            long currentTime = SystemClock.elapsedRealtime();

            // ตรวจจับการเขย่าด้วย Threshold และ Cooldown ป้องกันการนับเบิ้ล
            if (dynamicAcceleration > SHAKE_THRESHOLD && currentTime - lastShakeTime >= SHAKE_COOLDOWN_MS) {
                sessionShakes++;
                lastShakeTime = currentTime;

                if (tvShakes != null) {
                    tvShakes.setText("Shakes: " + sessionShakes);
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ไม่จำเป็นต้องใช้งาน
    }

    // =========================================================================
    // Activity Lifecycle
    // =========================================================================

    @Override
    protected void onResume() {
        super.onResume();

        if (hasActivityRecognitionPermission()) {
            registerStepSensor();
        }
        registerAccelerometer();

        loadActivityHistory();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager != null) {
            if (stepSensorRegistered) {
                sensorManager.unregisterListener(this, stepCounterSensor);
                stepSensorRegistered = false;
            }
            if (accelerometerRegistered) {
                sensorManager.unregisterListener(this, accelerometerSensor);
                accelerometerRegistered = false;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(timerRunnable);

        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // =========================================================================
    // Getter Methods สำหรับเรียกดูสถานะหรือทดสอบ
    // =========================================================================

    public int getCurrentSessionSteps() {
        return sessionSteps;
    }

    public int getCurrentSessionShakes() {
        return sessionShakes;
    }

    public int getCurrentSessionDurationSeconds() {
        return (int) (sessionDuration / 1000);
    }

    public double getCurrentSessionStepPace() {
        return currentStepPace;
    }

    public double getCurrentSessionShakePace() {
        return currentShakePace;
    }

    public boolean isSessionRunning() {
        return isSessionRunning;
    }
}
