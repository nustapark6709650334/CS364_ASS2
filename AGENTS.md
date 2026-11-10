# AGENTS.md — กฎการทำงานและขอบเขตสำหรับ AI Agent

เอกสารนี้กำหนดข้อตกลงสำหรับ AI Coding Assistant ที่เข้ามาช่วยพัฒนาโปรเจกต์ **CS364 Assignment 2 (StepByStep ActivityTracker)**

---

## 🚨 กฎเหล็ก (Core Rule)
> **"Implement only the requested scope."**
> (ทำเฉพาะขอบเขตที่ Issue หรือผู้ใช้ร้องขอเท่านั้น ห้ามขยาย Scope หรือแก้ไขไฟล์นอกเหนือจากที่ระบุโดยพลการ)

---

## 1. ขอบเขตของไฟล์และชื่อคลาสที่ล็อกตามเกณฑ์การตรวจของ TA
อาจารย์และพี่ TA ใช้ตารางตรวจคะแนนที่ล็อกชื่อไฟล์และแพ็กเกจไว้ที่ `com.example.ass2` ดังนั้น **ห้ามเปลี่ยนชื่อไฟล์หรือย้ายแพ็กเกจ** ของไฟล์ต่อไปนี้เด็ดขาด:

1. **พาร์ท Sensor (50% — Issue #2):**
   * `app/src/main/java/com/example/ass2/MainActivity.java`
   * `app/src/main/res/layout/activity_main.xml`
   * `app/src/main/AndroidManifest.xml`
   * **ข้อกำหนดสำคัญ:** ต้องใช้ `Sensor.TYPE_STEP_COUNTER` สำหรับนับก้าว และใช้ Accelerometer สำหรับตรวจจับการเขย่า (Shake Detection)

2. **พาร์ท SQLite (30% — Issue #3):**
   * `app/src/main/java/com/example/ass2/DBHelper.java`
   * **ข้อกำหนดสำคัญ:** สืบทอดจาก `SQLiteOpenHelper` รองรับการเพิ่มข้อมูลรอบกิจกรรม (Insert), ดึงประวัติทั้งหมด (Query), และลบรายการพร้อมคำนวณสถิติใหม่ (Delete & Recalculate)

3. **พาร์ท RecyclerView & Dialog (20% — Issue #4):**
   * `app/src/main/java/com/example/ass2/ActivityAdapter.java`
   * `app/src/main/res/layout/item_activity.xml`
   * `app/src/main/res/layout/dialog_activity_detail.xml`
   * **ข้อกำหนดสำคัญ:** แสดงการ์ดประวัติกิจกรรม พร้อมป้าย `NEW STEP RECORD` / `NEW SHAKE RECORD`, แตะรายการเพื่อเปิด Dialog รายละเอียด และรองรับการแสดง Empty State เมื่อไม่มีข้อมูล

---

## 2. การแยกสถานะของงาน (Separation of Concerns)
ให้แยกสถานะทั้ง 4 ส่วนออกจากกันอย่างเคร่งครัด:
1. **Requirement:** ยึดตาม Acceptance Criteria ใน GitHub Issue (#2, #3, #4, #5) และตาราง Checklist 10 ข้อเท่านั้น
2. **Code:** เขียนด้วยภาษา **Java** (ตามโครงสร้างโปรเจกต์ `MainActivity.java`) รักษาความสะอาดของโค้ดและไม่ลบโค้ดพาร์ทของสมาชิกคนอื่น
3. **CI / Verification:** ต้องสามารถคอมไพล์ผ่านคำสั่ง `./gradlew testDebugUnitTest`, `./gradlew lintDebug`, และ `./gradlew assembleDebug` ได้โดยไม่เกิด Error
4. **Pull Request:** ทุกการเปลี่ยนแปลงต้องส่งผ่าน Branch ย่อย (`feature/<issue>-...`, `fix/<issue>-...`, `docs/<issue>-...`) และอ้างอิง `Closes #<issue>` หรือ `Refs #<issue>` เสมอ
