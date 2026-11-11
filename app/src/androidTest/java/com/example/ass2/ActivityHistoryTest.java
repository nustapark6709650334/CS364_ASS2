package com.example.ass2;

import android.view.View;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ActivityHistoryTest {
    @Test
    public void savedHistorySurvivesRecreationAndDialogDeletesFromDatabase() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            androidx.test.platform.app.InstrumentationRegistry.getInstrumentation()
                    .getUiAutomation().grantRuntimePermission("com.example.ass2",
                            android.Manifest.permission.ACTIVITY_RECOGNITION);
        }
        long[] ids = {-1, -1};
        int[] baseline = {0};
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            try {
                scenario.onActivity(activity -> {
                    baseline[0] = activity.getActivityList().size();
                    int steps = activity.getDbHelper().getMaxSteps() + 10;
                    int shakes = activity.getDbHelper().getMaxShakes() + 10;
                    ids[0] = activity.saveCurrentSession(steps, shakes, 60, steps, shakes).getId();
                    ids[1] = activity.saveCurrentSession(steps + 10, shakes + 10, 120,
                            (steps + 10) / 2.0, (shakes + 10) / 2.0).getId();
                    assertTrue(ids[0] > 0);
                    assertTrue(ids[1] > 0);
                });
                scenario.recreate();
                scenario.onActivity(activity -> {
                    RecyclerView list = activity.findViewById(R.id.history_list);
                    ActivityAdapter adapter = (ActivityAdapter) list.getAdapter();
                    assertNotNull(adapter);
                    assertEquals(baseline[0] + 2, adapter.getItemCount());
                    assertEquals(View.GONE, activity.findViewById(R.id.history_empty).getVisibility());
                    assertTrue(activity.getDbHelper().getActivityById(ids[1]).isNewStepRecord());
                    // Bind a fresh card, exercising the same click listener as a visible row.
                    ActivityAdapter.Holder holder = adapter.onCreateViewHolder(list, 0);
                    adapter.onBindViewHolder(holder, 0);
                    assertEquals(View.VISIBLE, holder.stepRecord.getVisibility());
                    holder.itemView.performClick();
                });
                onView(withText(R.string.delete_activity)).perform(click());
                scenario.onActivity(activity -> {
                    assertNull(activity.getDbHelper().getActivityById(ids[1]));
                    assertTrue(activity.getDbHelper().getActivityById(ids[0]).isNewStepRecord());
                    assertTrue(activity.getDbHelper().getActivityById(ids[0]).isNewShakeRecord());
                    RecyclerView list = activity.findViewById(R.id.history_list);
                    assertEquals(baseline[0] + 1, list.getAdapter().getItemCount());
                    activity.deleteActivityRecord(ids[0]);
                    assertEquals(baseline[0], list.getAdapter().getItemCount());
                    assertEquals(baseline[0] == 0 ? View.VISIBLE : View.GONE,
                            activity.findViewById(R.id.history_empty).getVisibility());
                });
            } finally {
                scenario.onActivity(activity -> {
                    for (long id : ids) {
                        if (id > 0) activity.deleteActivityRecord(id);
                    }
                });
            }
        }
    }
}
