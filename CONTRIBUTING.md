# คู่มือการทำงานร่วมกันในทีม (Contributing Guidelines)

เอกสารนี้กำหนดมาตรฐานการแบ่งงาน การตั้งชื่อ Branch และการส่ง Pull Request สำหรับโปรเจกต์ **CS364 Assignment 2 (StepByStep ActivityTracker)** เพื่อให้ขอบเขตงาน (Scope) ของสมาชิกแต่ละคนชัดเจนและไม่เกิดปัญหาโค้ดชนกัน (Merge Conflict)

---

## 1. หลักการแบ่งงาน (Scope ของ Issue และ Branch)

* **"หนึ่ง Branch ควรรับผิดชอบหนึ่ง Issue หรือหนึ่งงานหลัก"** เพื่อให้ Scope ชัดเจนและตรวจสอบได้ง่าย
* ห้ามแก้ไขโค้ดข้ามส่วนของเพื่อนโดยไม่จำเป็น หากต้องแก้ไขไฟล์ร่วมกัน (เช่น `MainActivity.java`) ให้แก้ไขเฉพาะเมธอดหรือส่วนที่เกี่ยวข้องกับ Issue ของตนเองเท่านั้น

### รูปแบบการตั้งชื่อ Branch (Branch Naming Convention)
ตั้งชื่อ Branch โดยระบุประเภทงานและเลข Issue เสมอ:
* `feature/<issue-number>-<short-description>` — สำหรับพัฒนาฟีเจอร์ตามพาร์ทที่ได้รับมอบหมาย
* `fix/<issue-number>-<short-description>` — สำหรับแก้ไขบั๊ก
* `docs/<issue-number>-<short-description>` — สำหรับอัปเดตเอกสาร `README.md`, รูปภาพ หรือลิงก์วิดีโอ

#### ตัวอย่างการแตก Branch สำหรับการบ้านนี้:
| Issue | รายละเอียดงาน | ชื่อ Branch ที่แนะนำ |
| :---: | :--- | :--- |
| **#2** | พาร์ท Sensor 50% (Checklist ข้อ 1–5) | `feature/2-sensor-tracking` |
| **#3** | พาร์ท SQLite 30% (Checklist ข้อ 6–8) | `feature/3-sqlite-dbhelper` |
| **#4** | พาร์ท RecyclerView 20% (Checklist ข้อ 9–10) | `feature/4-recyclerview-adapter` |
| **#5** | สรุปรายชื่อสมาชิก, เอกสาร และวิดีโอสาธิต | `docs/5-submission-readme` |

---

## 2. การอ้างอิง Issue และเกณฑ์การตรวจรับงาน (Acceptance Criteria)

แต่ละ Issue บน GitHub มีการระบุ **Acceptance Criteria** ที่อ้างอิงจากตาราง Excel Checklist ของอาจารย์ไว้ชัดเจน:

1. **เมื่อทำเสร็จครบตามเกณฑ์ของ Issue นั้นแล้ว:**
   ให้ระบุในรายละเอียดของ Pull Request ว่า:
   ```markdown
   Closes #<issue-number>
   ```
   *(เช่น `Closes #2` เพื่อให้ GitHub ปิด Issue นั้นให้อัตโนมัติทันทีที่กด Merge)*

2. **หากทำเสร็จเพียงบางส่วน (ยังไม่ครบทุกข้อใน Issue):**
   ให้ระบุในรายละเอียดของ Pull Request ว่า:
   ```markdown
   Refs #<issue-number>
   ```
   พร้อมระบุรายการที่ทำเสร็จแล้ว และงานที่ยังเหลือค้างไว้

---

## 3. ขั้นตอนการส่งงานขึ้น GitHub (Pull Request Workflow)

1. ดึงโค้ดล่าสุดจาก `main` เสมอ:
   ```bash
   git checkout main
   git pull origin main
   ```
2. สร้าง Branch ใหม่ตามเลข Issue ที่รับผิดชอบ:
   ```bash
   git checkout -b feature/2-sensor-tracking
   ```
3. พัฒนาโค้ดและทดสอบการ Build ในเครื่อง:
   ```bash
   ./gradlew assembleDebug
   ```
4. Commit และ Push ขึ้น GitHub:
   ```bash
   git add .
   git commit -m "feat(sensor): implement step counter and shake detection (#2)"
   git push -u origin feature/2-sensor-tracking
   ```
5. เปิด **Pull Request** บน GitHub เข้าหา branch `main`:
   * กรอกข้อมูลตาม `.github/pull_request_template.md` ให้ครบถ้วน
   * ติ๊กยืนยันว่า **ผลลัพธ์ตรงตาม Acceptance Criteria ของ Issue** และ **ไม่มีการเปลี่ยนแปลงที่ไม่เกี่ยวข้อง**
   * รอให้ GitHub Actions (`regression` และ `observed-schema-upgrade`) ขึ้นสถานะผ่านสีเขียว ✅ ก่อนทำการ Merge
