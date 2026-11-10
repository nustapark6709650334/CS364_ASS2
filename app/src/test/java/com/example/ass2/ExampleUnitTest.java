package com.example.ass2;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit Test สำหรับทดสอบการทำงานของโมเดล ActivityRecord ใน DBHelper
 */
public class ExampleUnitTest {

    @Test
    public void testActivityRecord_creationAndGetters() {
        DBHelper.ActivityRecord record = new DBHelper.ActivityRecord(
                1L,
                "2026-11-10 21:00:00",
                1250,
                45,
                630,
                "10:30",
                119.0,
                4.2,
                true,
                false
        );

        assertEquals(1L, record.getId());
        assertEquals("2026-11-10 21:00:00", record.getTimestamp());
        assertEquals(1250, record.getSteps());
        assertEquals(45, record.getShakes());
        assertEquals(630, record.getDurationSeconds());
        assertEquals("10:30", record.getDurationFormatted());
        assertEquals(119.0, record.getStepPace(), 0.001);
        assertEquals(4.2, record.getShakePace(), 0.001);
        assertTrue(record.isNewStepRecord());
        assertFalse(record.isNewShakeRecord());
        assertTrue(record.hasAnyRecordBadge());
    }

    @Test
    public void testActivityRecord_summaryText() {
        DBHelper.ActivityRecord record = new DBHelper.ActivityRecord();
        record.setSteps(500);
        record.setShakes(20);
        assertEquals("500 ก้าว • เขย่า 20 ครั้ง", record.getSummaryText());
    }

    @Test
    public void testActivityRecord_paceText() {
        DBHelper.ActivityRecord record = new DBHelper.ActivityRecord();
        record.setStepPace(100.5);
        record.setShakePace(15.0);
        assertTrue(record.getPaceText().contains("100.5"));
        assertTrue(record.getPaceText().contains("15.0"));
    }
}
