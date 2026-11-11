## 📌 สรุปสิ่งที่ทำใน PR นี้ (Summary)
- **ผู้รับผิดชอบ (ชื่อ-รหัสนักศึกษา):** รอผู้ส่ง PR กรอก
- **พาร์ทที่พัฒนาใน PR นี้:**
  - [ ] 🏃 Sensor & Activity Calculation (50% — Issue #2)
  - [ ] 💾 SQLite Database (30% — Issue #3)
  - [x] 📱 RecyclerView & Dialog UI (20% — Issue #4)
  - [ ] 📄 เอกสาร / วิดีโอสาธิต / แก้ไขบั๊ก (Issue #5)

เพิ่มหน้าประวัติจาก SQLite จริงผ่าน ActivityAdapter แสดงวันเวลา จำนวนก้าว/เขย่า ระยะเวลา ค่าเฉลี่ย และป้ายสถิติ แตะการ์ดเพื่อดูรายละเอียดและลบกิจกรรม โดยโหลดรายการและ Empty State ใหม่หลังบันทึกหรือลบ

## 🔗 Related Issue
<!-- ระบุ Closes #<issue-number> เมื่อทำครบตามเกณฑ์ หรือ Refs #<issue-number> หากทำบางส่วน -->
Closes #4

## 📂 ไฟล์ที่แก้ไข/เพิ่มเติม (ตามเกณฑ์ตรวจของ TA)
- [x] `app/src/main/java/com/example/ass2/MainActivity.java`
- [ ] `app/src/main/java/com/example/ass2/DBHelper.java`
- [x] `app/src/main/java/com/example/ass2/ActivityAdapter.java`
- [x] `app/src/main/res/layout/activity_main.xml`
- [x] `app/src/main/res/layout/item_activity.xml`
- [x] `app/src/main/res/layout/dialog_activity_detail.xml`

## ✅ Verification (การตรวจสอบขอบเขตและคุณภาพงาน)
- [x] ทดสอบการทำงานในเครื่อง/Emulator แล้วผ่าน
- [x] **ผลลัพธ์ตรงตาม Acceptance Criteria ของ Issue**
- [x] **ตรวจสอบว่าไม่มีการเปลี่ยนแปลงที่ไม่เกี่ยวข้อง (ควบคุม Scope ของ Issue ไม่ให้บวม)**
- [ ] โค้ด Build ผ่าน (`./gradlew assembleDebug` ผ่านใน GitHub Actions)

ผลตรวจในเครื่อง: testDebugUnitTest, lintDebug และ assembleDebug ผ่าน; connectedDebugAndroidTest ผ่าน 2 tests บน Pixel 6a API 35 หลังรันซ้ำเมื่อเครื่องจำลองหลุดการเชื่อมต่อครั้งแรก

ActivityHistoryTest ตรวจการโหลดประวัติหลังสร้าง Activity ใหม่ การเปิด Dialog และลบจาก SQLite การย้ายป้ายสถิติ และ Empty State โดยล้างเฉพาะรายการที่ test สร้าง

ไฟล์เพิ่มเติม: app/build.gradle (RecyclerView dependency), strings.xml และ ActivityHistoryTest.java

วิธีทดลอง: Start Session → Stop Session → เลื่อนลงประวัติกิจกรรม → แตะการ์ด → ลบกิจกรรม

## 📋 Checklist การตรวจสอบตามเกณฑ์ Assignment 2
ข้อ 1–8 เป็นงานจาก main ที่เชื่อมใช้งานใน PR นี้ ไม่ได้ยืนยันการทดสอบเซนเซอร์จริงซ้ำ

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
- [x] **ข้อ 9 RecyclerView List:** แสดงรายการประวัติกิจกรรมใน `RecyclerView` ด้วย `item_activity.xml`
- [x] **ข้อ 10 Item Details & Empty State:** แตะรายการแล้วแสดง `dialog_activity_detail.xml` และแสดงข้อความเมื่อยังไม่มีประวัติกิจกรรม

## 📸 ภาพหน้าจอ / วิดีโอผลการทดสอบ (Screenshots / Video)
ยังไม่ได้แนบภาพ/วิดีโอ สำหรับชุดสาธิตส่งงานให้ดำเนินการต่อใน Issue #5

## 🔍 Reviewer Checklist (สำหรับเพื่อนในกลุ่มตรวจก่อน Merge)
- [ ] ตรวจสอบโค้ดและเนื้อหาตรงตาม Scope ของ Issue แล้ว
- [ ] ทดสอบตามขั้นตอนใน PR แล้ว
- [ ] พร้อม Merge เข้า `main`
