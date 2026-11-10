## 📌 สรุปสิ่งที่ทำใน PR นี้ (Summary)
- **ผู้รับผิดชอบ (ชื่อ-รหัสนักศึกษา):** 
- **พาร์ทที่พัฒนาใน PR นี้:**
  - [ ] 🏃 Sensor & Activity Calculation (50% — Issue #2)
  - [ ] 💾 SQLite Database (30% — Issue #3)
  - [ ] 📱 RecyclerView & Dialog UI (20% — Issue #4)
  - [ ] 📄 เอกสาร / วิดีโอสาธิต / แก้ไขบั๊ก (Issue #5)

## 🔗 Related Issue
<!-- ระบุ Closes #<issue-number> เมื่อทำครบตามเกณฑ์ หรือ Refs #<issue-number> หากทำบางส่วน -->
Closes #

## 📂 ไฟล์ที่แก้ไข/เพิ่มเติม (ตามเกณฑ์ตรวจของ TA)
- [ ] `app/src/main/java/com/example/ass2/MainActivity.java`
- [ ] `app/src/main/java/com/example/ass2/DBHelper.java`
- [ ] `app/src/main/java/com/example/ass2/ActivityAdapter.java`
- [ ] `app/src/main/res/layout/activity_main.xml`
- [ ] `app/src/main/res/layout/item_activity.xml`
- [ ] `app/src/main/res/layout/dialog_activity_detail.xml`

## ✅ Verification (การตรวจสอบขอบเขตและคุณภาพงาน)
- [ ] ทดสอบการทำงานในเครื่อง/Emulator แล้วผ่าน
- [ ] **ผลลัพธ์ตรงตาม Acceptance Criteria ของ Issue**
- [ ] **ตรวจสอบว่าไม่มีการเปลี่ยนแปลงที่ไม่เกี่ยวข้อง (ควบคุม Scope ของ Issue ไม่ให้บวม)**
- [ ] โค้ด Build ผ่าน (`./gradlew assembleDebug` ผ่านใน GitHub Actions)

## 📋 Checklist การตรวจสอบตามเกณฑ์ Assignment 2
### ส่วนที่ 1: Sensor (50%) — `MainActivity.java` (Issue #2)
- [ ] **ข้อ 1 Step Counter Sensor:** ใช้ `Sensor.TYPE_STEP_COUNTER` และขอ Permission `ACTIVITY_RECOGNITION`
- [ ] **ข้อ 2 Shake Detection:** เขย่าอุปกรณ์แล้วตรวจจับและเพิ่มจำนวนครั้งที่เขย่าได้
- [ ] **ข้อ 3 Session Delta:** เริ่มกิจกรรมแล้วนับเฉพาะก้าว/เขย่าใหม่ของกิจกรรมนั้น
- [ ] **ข้อ 4 Duration & Average:** คำนวณเวลา (`MM:SS`) และค่าเฉลี่ย `steps/min`, `shakes/min` ถูกต้อง
- [ ] **ข้อ 5 Total & New Record:** แสดงยอดสะสมจากประวัติ และแสดงป้าย `NEW STEP RECORD` / `NEW SHAKE RECORD` เมื่อทำลายสถิติ

### ส่วนที่ 2: SQLite (30%) — `DBHelper.java` + `MainActivity.java` (Issue #3)
- [ ] **ข้อ 6 Insert Session:** กดปุ่ม "บันทึกกิจกรรม" แล้วข้อมูลบันทึกลง SQLite
- [ ] **ข้อ 7 Query & Persistence:** ปิดและเปิดแอปใหม่ ประวัติกิจกรรมยังแสดงอยู่ครบ
- [ ] **ข้อ 8 Delete & Recalculate:** ลบรายการแล้วข้อมูลถูกลบออกจาก SQLite และคำนวณยอดสะสม/สถิติใหม่ถูกต้อง

### ส่วนที่ 3: RecyclerView (20%) — `ActivityAdapter.java` + Layouts (Issue #4)
- [ ] **ข้อ 9 RecyclerView List:** แสดงรายการประวัติกิจกรรมใน `RecyclerView` ด้วย `item_activity.xml`
- [ ] **ข้อ 10 Item Details & Empty State:** แตะรายการแล้วแสดง `dialog_activity_detail.xml` และแสดงข้อความเมื่อยังไม่มีประวัติกิจกรรม

## 📸 ภาพหน้าจอ / วิดีโอผลการทดสอบ (Screenshots / Video)
<!-- แนบรูปภาพหน้าจอหรือคลิปการรันบนเครื่องจริง/Emulator -->

## 🔍 Reviewer Checklist (สำหรับเพื่อนในกลุ่มตรวจก่อน Merge)
- [ ] ตรวจสอบโค้ดและเนื้อหาตรงตาม Scope ของ Issue แล้ว
- [ ] ทดสอบตามขั้นตอนใน PR แล้ว
- [ ] พร้อม Merge เข้า `main`
