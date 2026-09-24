# คู่มือการใส่รูปภาพ Asset สำหรับเกม "มาริโอ้เถื่อน (Hardcore Mario)"

โฟลเดอร์นี้ถูกจัดเตรียมเป็น "Box" สำหรับใส่ไฟล์รูปภาพทั้งหมดของเกม โดยมีไฟล์แม่แบบ (Placeholder) นามสกุล `.png` สร้างไว้ให้ครบทุกจุดแล้ว คุณสามารถนำรูปภาพจริงมาวางทับ (Replace) ชื่อไฟล์เดิมได้ทันทีครับ!

---

## 1. ตัวละครผู้เล่น (Player: Mario เถื่อน)
**โฟลเดอร์:** `assets/characters/player/`
- **ขนาดแนะนำ:** 64 x 64 pixels (หรือ 48 x 48 / 96 x 96 แต่แนะนำให้เท่ากันทุกรูป)
- **ฟอร์แมต:** PNG (พื้นหลังโปร่งใส Transparent)

### 1.1 ทิศทางการเล็งปืน 8 ทิศทาง (8 Directions Aiming)
**โฟลเดอร์:** `assets/characters/player/8directions/`
| ชื่อไฟล์ | ทิศทางที่มอง/เล็งปืน | องศา (โดยประมาณ) |
|---|---|---|
| `aim_e.png` | **ขวา (East / Right)** | 0° (หรือ 360°) |
| `aim_ne.png` | **ขวาบน (North-East / Up-Right)** | 45° |
| `aim_n.png` | **บนตรง (North / Up)** | 90° |
| `aim_nw.png` | **ซ้ายบน (North-West / Up-Left)** | 135° |
| `aim_w.png` | **ซ้าย (West / Left)** | 180° |
| `aim_sw.png` | **ซ้ายล่าง (South-West / Down-Left)** | 225° |
| `aim_s.png` | **ล่างตรง (South / Down)** | 270° |
| `aim_se.png` | **ขวาล่าง (South-East / Down-Right)** | 315° |

### 1.2 ท่าทางอื่นๆ ของผู้เล่น (Player States)
- `assets/characters/player/idle.png` : ท่ายืนนิ่งปกติ
- `assets/characters/player/jump.png` : ท่ากระโดดลอยตัวกลางอากาศ
- `assets/characters/player/crouch.png` : ท่าย่อตัว / หมอบคลาน (Hitbox จะต่ำลงเพื่อหลบกระสุน)

---

## 2. ศัตรู (Enemy: Guard ทหาร Apex Syndicate)
**โฟลเดอร์:** `assets/characters/guard/`
- **ขนาดแนะนำ:** 64 x 64 pixels
- **ฟอร์แมต:** PNG (พื้นหลังโปร่งใส)

### 2.1 ทิศทางการเล็งปืน 8 ทิศทางของศัตรู
**โฟลเดอร์:** `assets/characters/guard/8directions/`
- `aim_e.png` (เล็งขวา)
- `aim_ne.png` (เล็งขวาบน)
- `aim_n.png` (เล็งบน)
- `aim_nw.png` (เล็งซ้ายบน)
- `aim_w.png` (เล็งซ้าย)
- `aim_sw.png` (เล็งซ้ายล่าง)
- `aim_s.png` (เล็งล่าง)
- `aim_se.png` (เล็งขวาล่าง)

### 2.2 ท่ายืนลาดตระเวนของศัตรู
- `assets/characters/guard/idle_right.png` : ท่ายืน/เดินหันขวา
- `assets/characters/guard/idle_left.png` : ท่ายืน/เดินหันซ้าย

---

## 3. กระสุนปืน (Projectiles)
**โฟลเดอร์:** `assets/projectiles/`
- **ขนาดแนะนำ:** 24 x 24 pixels หรือ 16 x 16 pixels
- **ฟอร์แมต:** PNG (พื้นหลังโปร่งใส)
- `bullet_player.png` : กระสุนปืนไรเฟิลของ Mario (เช่น ประกายไฟสีเหลือง-ทอง)
- `bullet_enemy.png` : กระสุนปืนของศัตรู Guard (เช่น ประกายเลเซอร์หรือกระสุนสีแดง)

---

## 4. ไอเทมตกพื้น (Items & Pickups)
**โฟลเดอร์:** `assets/items/`
- **ขนาดแนะนำ:** 32 x 32 pixels
- **ฟอร์แมต:** PNG (พื้นหลังโปร่งใส)
- `health_pack.png` : กล่องยา / กระเป๋าพยาบาลกาชาด (ฟื้นฟูเลือด HP)
- `ammo_box.png` : ลังกระสุนยุทธวิธี (เติมลูกกระสุนปืน)

---

## 5. บล็อกและสิ่งกีดขวาง (Environment & Platforms)
**โฟลเดอร์:** `assets/environment/`
- **ขนาดแนะนำ:** 48 x 48 pixels
- **ฟอร์แมต:** PNG
- `block_platform.png` : บล็อกแท่นกระโดดลอยฟ้า (สไตล์กล่องเหล็กหรือบล็อก Mario ลอยได้)
- `block_ground.png` : บล็อกพื้นดิน / พื้นศูนย์วิจัยด้านล่างสุด
- `block_metal.png` : บล็อกคานเหล็ก / ผนังป้องกันกระสุน
- `barrel.png` : ถังน้ำมัน / ถังสารเคมี (สิ่งกีดขวาง)
- `hazard_spikes.png` : ขวากหนาม / กับดักแหลมคม

---

## 6. ภาพพื้นหลังและฉาก (Backgrounds)
**โฟลเดอร์:** `assets/backgrounds/`
- **ขนาดแนะนำ:** 1280 x 720 pixels (หรือ 1920 x 1080)
- **ฟอร์แมต:** PNG หรือ JPG
- `bg_main.png` : ฉากหลังหลัก (เช่น อาคารศูนย์วิจัยพังทลาย, โครงสร้างเหล็ก, ซากรถถังตาม Storyboard)
- `bg_far.png` : ฉากท้องฟ้า/หมอกควันระยะไกล (สำหรับทำ Parallax Scrolling เลื่อนช้าๆ)

---

## 7. ส่วนติดต่อผู้ใช้ (UI & HUD)
**โฟลเดอร์:** `assets/ui/`
- `crosshair.png` : เป้าเล็งเมาส์ (ขนาดแนะนำ 32 x 32 pixels)
- `icon_health.png` : ไอคอนรูปหัวใจ/เลือด แสดงแถบ HP (24 x 24 pixels)
- `icon_ammo.png` : ไอคอนรูปลูกกระสุน แสดงจำนวนกระสุนในแม็กกาซีน (24 x 24 pixels)

---

### วิธีนำรูปของคุณมาใส่:
1. เตรียมรูปภาพนามสกุล `.png`
2. ก๊อปปี้ไปวางทับไฟล์ในโฟลเดอร์ที่ระบุไว้ด้านบน โดยใช้ชื่อเดิม
3. ตัวเกมจะดึงรูปใหม่ที่คุณใส่ไปแสดงผลทันทีโดยอัตโนมัติ!
