# StepByStep ActivityTracker — CS364 Assignment 2

แอปพลิเคชัน Android สำหรับนับก้าวเดิน (`TYPE_STEP_COUNTER`) และตรวจจับจำนวนครั้งในการเขย่าอุปกรณ์ (Accelerometer) พร้อมบันทึกสถิติและประวัติกิจกรรมลงฐานข้อมูล **SQLite** และแสดงผลผ่าน **RecyclerView**

---

## 👥 สมาชิกในกลุ่มและหน้าที่ความรับผิดชอบ (Team Members & Responsibilities)

> *ระบุหน้าที่ของสมาชิกทุกคนในกลุ่มตามคำสั่งแบบฝึกปฏิบัติที่ 2 (ข้อ 5)*

| ลำดับ | ชื่อ - นามสกุล | รหัสนักศึกษา | หน้าที่ความรับผิดชอบ (Role & Scope) | Issue ที่รับผิดชอบ |
| :---: | :--- | :---: | :--- | :---: |
| 1 | บูรพา จินดาเลิศ         | 6709650441 | พัฒนาระบบ Sensor นับก้าว, ตรวจจับการเขย่า, คำนวณเวลา/ค่าเฉลี่ย และสถิติสูงสุด (`MainActivity.java`) | [#2](https://github.com/nustapark6709650334/CS364_ASS2/issues/2) |
| 2 | กุลศิษฐ์ บัญชาพงศ์โอฬาร | 6709650185 | พัฒนาฐานข้อมูล SQLite สำหรับบันทึก ดึงข้อมูล และลบประวัติกิจกรรม (`DBHelper.java`) | [#3](https://github.com/nustapark6709650334/CS364_ASS2/issues/3) |
| 3 | กิติบดิณ เฮงมงคลสกุล    | 6709650169 | ออกแบบ UI, พัฒนา `RecyclerView` (`ActivityAdapter.java`), Dialog รายละเอียด และ Empty State | [#4](https://github.com/nustapark6709650334/CS364_ASS2/issues/4) |
| 4 | สุเมธา พวงทอง    | 6709650706 | จัดทำ README ระบุหน้าที่สมาชิกในกลุ่ม ติดตั้งและ ทดลองใช้งาน จัดทำวิดีโอ/ภาพหน้าจอสาธิตตาม Checklist  
---

## 🎥 วิดีโอสาธิตและภาพหน้าจอการทำงาน (Video Demo & Screenshots)

> *ตามคำสั่งข้อ 7: แสดงตัวอย่างการประมวลผลตาม Checklist เพื่อให้สะดวกต่อการตรวจสอบของพี่ TA*

- **🔗 ลิงก์วิดีโอสาธิตการทำงาน (Video Demo):** *(https://drive.google.com/file/d/1gIJjfbbAoC6Gsdiz-82TS0Xy0JThpJwz/view?usp=sharing)*

### ภาพหน้าจอตัวอย่าง (Screenshots)
| หน้าจอหลักขณะทำกิจกรรม (Current Session) | หน้าจอประวัติกิจกรรมและป้าย New Record | หน้าต่างรายละเอียด (Dialog Detail) |
| :---: | :---: | :---: |
| ![Current Session](CS364_ASS2/IMG_9876.PNG) | ![New Record](CS364_ASS2/IMG_9872.PNG) | ![Dialog Detail](CS364_ASS2/IMG_9873.PNG) |

---

## ✅ รายการตรวจสอบตามเกณฑ์ (Assignment 2 Checklist — 10 คะแนน)

| # | รายการตรวจสอบ | ตำแหน่งที่ตรวจ | วิธีตรวจอย่างรวดเร็ว | สัดส่วน | สถานะ |
| :-: | :--- | :--- | :--- | :-: | :-: |
| **1** | **Step Counter Sensor** | `MainActivity.java` | ตรวจว่าใช้ `TYPE_STEP_COUNTER` และแสดงจำนวนก้าวที่ตรวจจับได้ | Sensor (50%) | ✅ |
| **2** | **Shake Detection** | `MainActivity.java` | เขย่าอุปกรณ์แล้วตรวจว่าตรวจจับและเพิ่มจำนวนครั้งที่เขย่าได้ | Sensor (50%) | ✅ |
| **3** | **Session Delta** | `MainActivity.java` | เริ่มกิจกรรมแล้วตรวจว่านับเฉพาะจำนวนก้าว/ครั้งที่เขย่าใหม่ของกิจกรรมนั้น | Sensor (50%) | ✅ |
| **4** | **Duration & Average** | `MainActivity.java` | ตรวจเวลาทำกิจกรรม (`MM:SS`) และค่าเฉลี่ยก้าวต่อนาที/เขย่าต่อนาทีว่าคำนวณถูกต้อง | Sensor (50%) | ✅ |
| **5** | **Total & New Record** | `MainActivity.java` + App | ตรวจยอดรวมสะสมและป้ายสถิติสูงสุด (`NEW STEP RECORD` / `NEW SHAKE RECORD`) | Sensor (50%) | ✅ |
| **6** | **Insert Session** | `DBHelper.java` + `MainActivity.java` | กดปุ่ม "บันทึกกิจกรรม" แล้วตรวจว่าข้อมูลถูกเพิ่มลง SQLite | SQLite (30%) | ✅ |
| **7** | **Query & Persistence** | `DBHelper.java` + `MainActivity.java` | ปิดและเปิดแอปใหม่ แล้วตรวจว่าประวัติกิจกรรมที่บันทึกไว้ยังแสดงอยู่ | SQLite (30%) | ✅ |
| **8** | **Delete & Recalculate** | `DBHelper.java` + `MainActivity.java` | ลบรายการกิจกรรมแล้วตรวจว่าข้อมูลถูกลบและสถิติที่เกี่ยวข้องอัปเดตถูกต้อง | SQLite (30%) | ✅ |
| **9** | **RecyclerView List** | `ActivityAdapter.java` + `item_activity.xml` + `MainActivity.java` | ตรวจว่าประวัติกิจกรรมแสดงเป็นรายการใน `RecyclerView` ได้ถูกต้อง | RecyclerView (20%) | ✅ |
| **10** | **Item Details & Empty State** | `ActivityAdapter.java` + `dialog_activity_detail.xml` + App | แตะรายการเพื่อดูรายละเอียด และตรวจข้อความ/สถานะเมื่อยังไม่มีประวัติกิจกรรม | RecyclerView (20%) | ✅ |

---

## 📂 โครงสร้างไฟล์หลักของโปรเจกต์ (Project Structure)

```text
ASS2/
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/example/ass2/
│   │   ├── MainActivity.java          # ข้อ 1-8: จัดการ Sensor, เวลา, คำนวณสถิติ และเชื่อม DB/RecyclerView
│   │   ├── DBHelper.java              # ข้อ 6-8: จัดการฐานข้อมูล SQLite (Insert, Query, Delete)
│   │   └── ActivityAdapter.java       # ข้อ 9-10: Adapter สำหรับแสดงรายการประวัติใน RecyclerView
│   └── res/layout/
│       ├── activity_main.xml          # หน้าจอหลักของแอป StepByStep
│       ├── item_activity.xml          # ข้อ 9: Layout ของแต่ละการ์ดประวัติกิจกรรม
│       └── dialog_activity_detail.xml # ข้อ 10: Dialog แสดงรายละเอียดเมื่อแตะที่รายการ
├── CONTRIBUTING.md                    # ข้อกำหนดการแบ่งงาน, การตั้งชื่อ Branch และการเปิด PR
├── AGENTS.md                          # กฎควบคุมขอบเขตงานสำหรับ AI Agent
└── .github/
    ├── pull_request_template.md       # เทมเพลตตรวจสอบงานก่อน Merge
    └── workflows/android-ci.yml       # ระบบตรวจ Build & Checklist อัตโนมัติ (GitHub Actions)
```
