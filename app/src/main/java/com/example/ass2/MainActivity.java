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

public class MainActivity extends AppCompatActivity
        implements SensorEventListener {

    private static final int REQUEST_ACTIVITY_RECOGNITION = 1001;

    // Shake detection settings
    private static final float SHAKE_THRESHOLD = 12.0f;
    private static final long SHAKE_COOLDOWN_MS = 500;

    // Sensors
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private Sensor accelerometerSensor;

    private boolean stepSensorRegistered = false;
    private boolean accelerometerRegistered = false;

    // Database
    private DatabaseHelper databaseHelper;

    // UI
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

    // Session state
    private boolean isSessionRunning = false;

    private int latestTotalSteps = -1;
    private int sessionStartStepCount = -1;

    private int sessionSteps = 0;
    private int sessionShakes = 0;

    private long sessionStartTime = 0;
    private long sessionDuration = 0;
    private long lastShakeTime = 0;

    // Timer
    private final Handler handler =
            new Handler(Looper.getMainLooper());

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

        // Handle system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {

                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                });

        // Connect UI
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

        // Initialize database
        databaseHelper = new DatabaseHelper(this);

        // Load saved history
        updateTotalHistory();

        // Initial button states
        btnStopSession.setEnabled(false);

        btnStartSession.setOnClickListener(v -> startSession());
        btnStopSession.setOnClickListener(v -> stopSession());

        // Initialize SensorManager
        sensorManager = (SensorManager)
                getSystemService(SENSOR_SERVICE);

        // Initialize sensors
        stepCounterSensor = sensorManager.getDefaultSensor(
                Sensor.TYPE_STEP_COUNTER);

        accelerometerSensor = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        // Initial UI
        tvSteps.setText(stepCounterSensor == null
                ? "Step Counter unavailable"
                : "Steps: 0");

        tvShakes.setText(accelerometerSensor == null
                ? "Accelerometer unavailable"
                : "Shakes: 0");

        tvDuration.setText("Duration: 00:00");
        tvStepsPerMinute.setText("Steps/min: 0");
        tvShakesPerMinute.setText("Shakes/min: 0");

        // Request permission
        if (!hasActivityRecognitionPermission()) {
            requestActivityRecognitionPermission();
        }
    }

    // Check Activity Recognition permission
    private boolean hasActivityRecognitionPermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return true;
        }

        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    // Request permission
    private void requestActivityRecognitionPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACTIVITY_RECOGNITION
                    },
                    REQUEST_ACTIVITY_RECOGNITION
            );
        }
    }

    // Register Step Counter
    private void registerStepSensor() {

        if (stepCounterSensor == null || stepSensorRegistered) {
            return;
        }

        if (!hasActivityRecognitionPermission()) {
            return;
        }

        stepSensorRegistered = sensorManager.registerListener(
                this,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_NORMAL
        );

        if (!stepSensorRegistered) {
            tvSteps.setText("Unable to register Step Counter");
        }
    }

    // Register Accelerometer
    private void registerAccelerometer() {

        if (accelerometerSensor == null || accelerometerRegistered) {
            return;
        }

        accelerometerRegistered = sensorManager.registerListener(
                this,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_UI
        );

        if (!accelerometerRegistered) {
            tvShakes.setText("Unable to register Accelerometer");
        }
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);

        if (requestCode == REQUEST_ACTIVITY_RECOGNITION) {

            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                registerStepSensor();

            } else {
                tvSteps.setText(
                        "Activity Recognition permission denied");
            }
        }
    }

    // Start a new session
    private void startSession() {

        // Reset session data
        sessionSteps = 0;
        sessionShakes = 0;
        sessionDuration = 0;
        lastShakeTime = 0;

        // Save current device step count as baseline
        sessionStartStepCount = latestTotalSteps;

        // Start timer
        sessionStartTime = SystemClock.elapsedRealtime();

        isSessionRunning = true;

        // Reset UI
        tvSteps.setText("Steps: 0");
        tvShakes.setText("Shakes: 0");
        tvDuration.setText("Duration: 00:00");
        tvStepsPerMinute.setText("Steps/min: 0");
        tvShakesPerMinute.setText("Shakes/min: 0");

        // Clear previous record messages
        tvNewStepRecord.setText("");
        tvNewShakeRecord.setText("");

        // Update buttons
        btnStartSession.setEnabled(false);
        btnStopSession.setEnabled(true);

        // Start timer updates
        handler.removeCallbacks(timerRunnable);
        handler.post(timerRunnable);
    }

    // Stop current session
    private void stopSession() {

        if (!isSessionRunning) {
            return;
        }

        // Stop session
        isSessionRunning = false;

        // Stop timer updates
        handler.removeCallbacks(timerRunnable);

        // Calculate duration
        sessionDuration = SystemClock.elapsedRealtime()
                - sessionStartTime;

        updateDurationDisplay();

        // Calculate averages
        double durationMinutes = sessionDuration / 60000.0;

        if (durationMinutes > 0) {

            double stepsPerMinute =
                    sessionSteps / durationMinutes;

            double shakesPerMinute =
                    sessionShakes / durationMinutes;

            tvStepsPerMinute.setText(
                    String.format(
                            java.util.Locale.getDefault(),
                            "Steps/min: %.2f",
                            stepsPerMinute)
            );

            tvShakesPerMinute.setText(
                    String.format(
                            java.util.Locale.getDefault(),
                            "Shakes/min: %.2f",
                            shakesPerMinute)
            );
        }

        // Save session and check records
        saveSessionAndCheckRecords();

        // Update buttons
        btnStartSession.setEnabled(true);
        btnStopSession.setEnabled(false);
    }

    // Save session to SQLite and check new records
    private void saveSessionAndCheckRecords() {

        // Get previous records BEFORE saving current session
        int previousHighestSteps =
                databaseHelper.getHighestSteps();

        int previousHighestShakes =
                databaseHelper.getHighestShakes();

        // Check if current session breaks records
        boolean isNewStepRecord =
                sessionSteps > previousHighestSteps;

        boolean isNewShakeRecord =
                sessionShakes > previousHighestShakes;

        // Save completed session
        boolean saved = databaseHelper.saveSession(
                sessionSteps,
                sessionShakes,
                sessionDuration / 1000
        );

        if (!saved) {
            Toast.makeText(
                    this,
                    "Failed to save session",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Display new records
        if (isNewStepRecord && sessionSteps > 0) {

            tvNewStepRecord.setText("NEW STEP RECORD");
        }

        if (isNewShakeRecord && sessionShakes > 0) {

            tvNewShakeRecord.setText("NEW SHAKE RECORD");
        }

        // Refresh total history
        updateTotalHistory();

        Toast.makeText(
                this,
                "Session saved successfully",
                Toast.LENGTH_SHORT
        ).show();
    }

    // Update accumulated history from database
    private void updateTotalHistory() {

        int totalSteps = databaseHelper.getTotalSteps();
        int totalShakes = databaseHelper.getTotalShakes();

        tvTotalHistory.setText(
                "ยอดสะสมจากประวัติ "
                        + totalSteps
                        + " ก้าว • เขย่า "
                        + totalShakes
                        + " ครั้ง"
        );
    }

    // Update duration display
    private void updateDurationDisplay() {

        long elapsed;

        if (isSessionRunning) {
            elapsed = SystemClock.elapsedRealtime()
                    - sessionStartTime;
        } else {
            elapsed = sessionDuration;
        }

        long totalSeconds = elapsed / 1000;

        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        tvDuration.setText(
                String.format(
                        java.util.Locale.getDefault(),
                        "Duration: %02d:%02d",
                        minutes,
                        seconds)
        );
    }

    // Receive Sensor events
    @Override
    public void onSensorChanged(SensorEvent event) {

        // Step Counter
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

            latestTotalSteps = (int) event.values[0];

            if (isSessionRunning) {

                // Set baseline when first sensor value arrives
                if (sessionStartStepCount == -1) {
                    sessionStartStepCount = latestTotalSteps;
                }

                // Calculate session steps
                sessionSteps = Math.max(
                        0,
                        latestTotalSteps - sessionStartStepCount
                );

                tvSteps.setText("Steps: " + sessionSteps);
            }
        }

        // Accelerometer / Shake Detection
        else if (event.sensor.getType()
                == Sensor.TYPE_ACCELEROMETER) {

            if (!isSessionRunning) {
                return;
            }

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Calculate acceleration magnitude
            double magnitude = Math.sqrt(
                    x * x + y * y + z * z
            );

            // Remove gravity approximately
            double dynamicAcceleration = Math.abs(
                    magnitude - SensorManager.GRAVITY_EARTH
            );

            long currentTime = SystemClock.elapsedRealtime();

            // Detect shake with threshold and cooldown
            if (dynamicAcceleration > SHAKE_THRESHOLD
                    && currentTime - lastShakeTime
                    >= SHAKE_COOLDOWN_MS) {

                sessionShakes++;
                lastShakeTime = currentTime;

                tvShakes.setText(
                        "Shakes: " + sessionShakes);
            }
        }
    }

    @Override
    public void onAccuracyChanged(
            Sensor sensor, int accuracy) {
        // Not required
    }

    // Register sensors when Activity becomes active
    @Override
    protected void onResume() {
        super.onResume();

        if (hasActivityRecognitionPermission()) {
            registerStepSensor();
        }

        registerAccelerometer();
    }

    // Unregister sensors when Activity is paused
    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager != null) {

            if (stepSensorRegistered) {
                sensorManager.unregisterListener(
                        this, stepCounterSensor);

                stepSensorRegistered = false;
            }

            if (accelerometerRegistered) {
                sensorManager.unregisterListener(
                        this, accelerometerSensor);

                accelerometerRegistered = false;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(timerRunnable);

        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
