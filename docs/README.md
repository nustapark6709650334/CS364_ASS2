# เอกสารกำกับสถานะและการส่งมอบงาน (Documentation & Workflow Status)

โปรเจกต์ **CS364 Assignment 2 (StepByStep ActivityTracker)** แยกสถานะของการทำงานออกเป็น **4 สถานะ** อย่างเคร่งครัด เพื่อความชัดเจนในการติดตามความคืบหน้าของสมาชิกแต่ละคนในกลุ่ม:

---

## 1. Requirement (ข้อกำหนดและขอบเขตใน Issue)
ทุกฟีเจอร์ต้องเริ่มต้นจากข้อกำหนดใน GitHub Issues ที่อ้างอิงตามตาราง Excel Checklist ของอาจารย์:
* **Issue [#2](https://github.com/nustapark6709650334/CS364_ASS2/issues/2):** พาร์ท Sensor & Activity Calculation (50% — Checklist ข้อ 1–5)
* **Issue [#3](https://github.com/nustapark6709650334/CS364_ASS2/issues/3):** พาร์ท SQLite Database (30% — Checklist ข้อ 6–8)
* **Issue [#4](https://github.com/nustapark6709650334/CS364_ASS2/issues/4):** พาร์ท RecyclerView & Dialog UI (20% — Checklist ข้อ 9–10)
* **Issue [#5](https://github.com/nustapark6709650334/CS364_ASS2/issues/5):** เอกสารสรุปหน้าที่สมาชิกในกลุ่ม และวิดีโอสาธิตการทำงาน

---

## 2. Code (การพัฒนาโค้ดตาม Scope)
* พัฒนาโค้ดเฉพาะภายในขอบเขตของ Issue ที่ได้รับมอบหมาย (`Implement only the requested scope`)
* แยก Branch ตามรูปแบบใน [CONTRIBUTING.md](../CONTRIBUTING.md) (`feature/<issue>-...`, `fix/<issue>-...`, `docs/<issue>-...`)
* รักษาชื่อคลาสและไฟล์ตามที่ตารางตรวจของ TA กำหนด (`MainActivity.java`, `DBHelper.java`, `ActivityAdapter.java`, `item_activity.xml`, `dialog_activity_detail.xml`)

---

## 3. Deployment / Build Artifact (การสร้างไฟล์ติดตั้งแอป `.apk`)
* ทุกครั้งที่มีการเปิด Pull Request หรือ Merge เข้า `main` ระบบ GitHub Actions ([android-ci.yml](../.github/workflows/android-ci.yml)) จะทำหน้าที่คอมไพล์และสร้างไฟล์ **`app-debug.apk`** อัตโนมัติ
* สมาชิกในกลุ่มและพี่ TA สามารถดาวน์โหลดไฟล์ `app-debug-apk` จากส่วน **Artifacts** ของ GitHub Actions ไปติดตั้งบนสมาร์ทโฟนหรือ Emulator ได้ทันที

---

## 4. Verification (การตรวจสอบและยืนยันผลลัพธ์)
งานจะถือว่าเสร็จสมบูรณ์ (Done) และพร้อมปิด Issue (`Closes #<issue>`) เมื่อผ่านการตรวจสอบทั้ง 2 ระดับ:
1. **Automated Verification (CI Checks):**
   * ผ่านการตรวจ `regression` (Unit Test, Android Lint, และ Build Debug APK)
   * ผ่านการตรวจ `observed-schema-upgrade` (ตรวจสอบโครงสร้างไฟล์และ `DBHelper.java`)
2. **Manual Verification (Runtime & Acceptance Criteria):**
   * ทดสอบรันจริงบนสมาร์ทโฟนหรือ Emulator ตรงตาม Acceptance Criteria ของ Issue ทุกข้อ
   * แนบหลักฐานภาพหน้าจอ (Screenshots) หรือวิดีโอสาธิตใน Pull Request และใน [README.md](../README.md)
