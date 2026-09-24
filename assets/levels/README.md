# 🗺️ Level Design & Stage XML Documentation

โฟลเดอร์นี้รวบรวมไฟล์ออกแบบด่านทั้งหมดของเกม **Hardcore Mario (มาริโอ้เถื่อน)** ที่สร้างจาก **draw.io (Diagrams.net)**

---

## 📁 ไฟล์ด่านในโฟลเดอร์นี้

| ไฟล์ | ด่าน | คำอธิบาย |
|---|---|---|
| `hardcore_mario_stage1.drawio.xml` | Stage 1 | ด่านศูนย์วิจัยชั้นนอก (Outer Labs) - แพลตฟอร์มกระโดด, หลุมหนาม, บล็อกทำลายได้ และทหารเฝ้ายาม |
| `hardcore_mario_stage2.drawio.xml` | Stage 2 | ด่านศูนย์วิจัยชั้นลึก (Inner Labs) - สิ่งกีดขวางเพดานหนามที่ต้องกระโดดและย่อตัวกลางอากาศ (`W` + `S`) |

---

## 📐 กฎและมาตราส่วนการออกแบบด่าน (Level Specifications)

1. **มาตราส่วนพิกัด (Grid Scale):**
   - ใน **draw.io**: ใช้ขนาดช่องมาตรฐาน `20 x 20` px
   - ใน **ตัวเกม (In-Game Engine)**: อัตราส่วนขยาย `SCALE = 2.0` (1 บล็อก draw.io = `40 x 40` px ในเกม)
2. **องค์ประกอบและรูปทรง (Legend Mapping):**
   - 👤 `value="player"` : ตำแหน่งเกิดของผู้เล่น (Spawn Point)
   - 💂 `value="enemy"` : ตำแหน่งเกิดของทหารศัตรู Guard
   - 🌀 `value="exit"` หรือ `shape=loopLimit` : ประตูวาร์ปจบด่าน / ไปด่านถัดไป
   - ➕ `shape=cross` : กล่องยาพยาบาล (Health Pack)
   - 🔺 `triangle` (ปกติ `rotation=-90`): หนามแหลมบนพื้น (Floor Spikes)
   - 🔻 `triangle` (กลับหัว `flipV=1; flipH=1;`): หนามแหลมห้อยจากเพดาน (Ceiling Spikes)
   - 🟧 บล็อกสีส้ม (`#FF8000`): บล็อกอิฐที่ผู้เล่นสามารถยิงทำลายได้ (Breakable Block)
   - 🟦 บล็อกสีน้ำเงิน (`#0050EF`): บล็อกคอนกรีต/เหล็กที่ทำลายไม่ได้ (Solid Block)
   - ⬛ แพลตฟอร์มยาวด้านล่าง: พื้นดินหลัก (Ground Platform)

---

## 🚀 วิธีเพิ่มด่านใหม่ (How to Add a New Stage)

1. สร้างหรือก็อปปี้ไฟล์ `.drawio.xml` ตั้งชื่อเป็น `hardcore_mario_stage<เลขด่าน>.drawio.xml` เช่น `hardcore_mario_stage3.drawio.xml`
2. วางไฟล์ไว้ในโฟลเดอร์ `assets/levels/`
3. เมื่อผู้เล่นเดินเข้าประตู `EXIT` ของด่านก่อนหน้า ระบบเกมจะค้นหาและโหลดด่านถัดไปให้อัตโนมัติทันที
