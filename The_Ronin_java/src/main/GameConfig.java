package main;

/**
 * ค่าคงที่ที่เกี่ยวกับขนาด/สเกล รวมไว้ที่เดียว (single source of truth)
 *
 * ค่าที่เป็นตัวเลขดิบในไฟล์นี้มีสองประเภท
 *   - xxx_PX  = ขนาดในหน่วย "พิกเซลของรูปต้นฉบับ" (วัดจากไฟล์ png ไม่เปลี่ยนตาม SCALE)
 *   - ที่เหลือ = ขนาดในหน่วย "พิกเซลของโลกเกม" คำนวณจาก xxx_PX × SCALE ให้เอง
 * ดังนั้นอยากขยายตัวละคร แก้ SCALE ตัวเดียวพอ
 */
public final class GameConfig {
    private GameConfig() { }

    // =====================================================
    // สเกลของ pixel art  <<<  แก้ตรงนี้ที่เดียว (ใช้จำนวนเต็ม 2, 3, 4)
    // =====================================================
    public static final int SCALE = 5;

    // ---------- ตัวละคร (วัดจาก Player.png / Ashigaru.png ขนาด 32x32) ----------
    public static final int SPRITE_PX = 32;
    public static final int BODY_CENTER_PX = 14;   // กึ่งกลางลำตัวนับจากซ้ายของรูป (ลำตัวอยู่ px 8–20)
    public static final int BODY_WIDTH_PX = 12;
    public static final int HITBOX_PAD_PX = 2;     // ส่วนเผื่อ hitbox ให้กว้างกว่าลำตัวนิดหน่อย
    public static final int HAND_Y_PX = 20;        // ความสูงของมือจากหัวรูป (อาวุธถูกวาดไว้ระดับนี้)
    public static final int MUZZLE_PX = 8;         // จุดปล่อยลูกธนูห่างจากกึ่งกลางตัว

    public static final int SPRITE = SPRITE_PX * SCALE;                  // ขนาดรูปบนจอ
    public static final int BODY_CENTER = BODY_CENTER_PX * SCALE;
    public static final int HITBOX_W = (BODY_WIDTH_PX + HITBOX_PAD_PX) * SCALE;
    public static final int HITBOX_H = SPRITE;
    public static final int HAND_Y = HAND_Y_PX * SCALE;
    public static final int MUZZLE = MUZZLE_PX * SCALE;

    // ---------- Effect / ลูกธนู / ของดรอป / UI ----------
    public static final int SLASH_SCALE = SCALE * 3 / 2;   // effect ฟันใหญ่กว่าตัวละครเล็กน้อย
    public static final int LUNGE = 4 * SCALE;             // ระยะที่ทวนพุ่งไปข้างหน้าตอนแทง
    public static final int ARROW_SCALE = SCALE;
    public static final int ARROW_HALF_LENGTH = 5 * SCALE; // ครึ่งความยาวลูกธนู (ใช้หาปลายหัว)
    public static final int ARROW_TIP_SIZE = 5 * SCALE;    // hitbox ที่ปลายหัวลูกธนู
    public static final int DROP_W = 24 * SCALE;           // บริเวณที่เก็บอาวุธได้
    public static final int DROP_H = 12 * SCALE;
    public static final int HEALTHBAR_W = 20 * SCALE;
    public static final int ITEM_W = 14 * SCALE;           // บริเวณที่เก็บไอเทมได้ (ยา/เกราะ)
    public static final int ITEM_H = 14 * SCALE;
    public static final long SMOKE_MS = 700;               // ควันวาร์ป fade in แล้ว fade out รวมกี่ ms
    public static final int SMOKE_SCALE = SCALE;
    public static final float BUFF_TINT = 0.5f;            // สีแดงทับ effect/ลูกธนูตอนมียาพลังโจมตี (0.5 = 50%)

    // =====================================================
    // หน้าจอและพื้น (ไม่ขึ้นกับ SCALE ของตัวละคร)
    // =====================================================
    public static final int TILE_SIZE = 64;
    public static final int SCREEN_COLUMNS = 16;
    public static final int SCREEN_ROWS = 12;
    public static final int SCREEN_WIDTH = SCREEN_COLUMNS * TILE_SIZE;
    public static final int SCREEN_HEIGHT = SCREEN_ROWS * TILE_SIZE;

    /** ตำแหน่งพื้นบนภาพแมพ (ถ้าเปลี่ยนแมพหรือความสูงจอ ต้องปรับให้ตรงกับพื้นในรูป) */
    public static final int GROUND_Y = 665;

    // =====================================================
    // แสง/ความมืด (ใช้กับแมพที่ลงทะเบียนไว้ใน Lighting.forMap)
    // =====================================================
    public static final float DARKNESS = 0.78f;          // ความทึบของความมืด: 0 = สว่างปกติ, 1 = มืดสนิท
    public static final int LANTERN_RADIUS = 280;        // รัศมี bubble แสงของโคมไฟ (px)
    public static final int PLAYER_LIGHT_RADIUS = 220;   // รัศมี bubble แสงรอบตัวผู้เล่น (0 = ไม่มี)

    // =====================================================
    // เมนู (งานศิลป์ที่วาดเอง) — ไฟล์รูปอยู่ใน res/ ไฟล์ไหนไม่มีจะวาดส่วนนั้นด้วยโค้ดแทน
    //
    // ตำแหน่งที่ลงท้าย _PX อยู่ในหน่วย "พิกเซลของรูปพื้นหลังหน้านั้น" (Menuscreen.png / weaBackground.png)
    // เปิดรูปในโปรแกรมวาดรูป ชี้เมาส์อ่านพิกัดมาใส่ได้เลย เกมขยายทุกอย่างตามที่พื้นหลังถูกขยายให้เต็มจอเอง
    // (พื้นหลังกว้าง 256 → ขยาย ×4, กว้าง 512 → ×2) ค่า -1 = ให้เกมจัดตำแหน่งให้อัตโนมัติ
    // =====================================================
    /** ใช้ตอนไม่มีรูปพื้นหลังให้อ้างอิง (ปกติสเกลมาจากรูปพื้นหลัง) */
    public static final float UI_SCALE_DEFAULT = 4f;
    /** true = วาดชื่อเกม/หัวข้อเป็นตัวหนังสือทับบนรูปที่วาดเอง (ปกติรูปมีชื่ออยู่แล้ว จึงปิดไว้) */
    public static final boolean UI_TITLE_TEXT_OVER_ART = false;

    /** หน้าเมนูหลัก: X = กึ่งกลางปุ่ม (-1 = กลางจอ), Y = ขอบบนของปุ่ม (-1 = อัตโนมัติ) */
    public static final int MENU_START_X_PX = -1, MENU_START_Y_PX = -1;
    public static final int MENU_EXIT_X_PX  = -1, MENU_EXIT_Y_PX  = -1;
    /** ช่องว่างใต้ปุ่ม START ตอน MENU_EXIT_Y_PX = -1 */
    public static final int MENU_BUTTON_GAP_PX = 4;

    /** หน้าเลือกอาวุธ: มุมซ้ายบนของ weaFrame (-1 = กึ่งกลางแนวนอน / ชิดล่าง เว้นขอบล่างให้เอง) */
    public static final int WEA_FRAME_X_PX = -1, WEA_FRAME_Y_PX = -1;
    /** ระยะห่างระหว่าง weaInnerFrame แต่ละใบ (-1 = แบ่งเท่าๆ กันให้เต็มกรอบ) */
    public static final int WEA_INNER_GAP_PX = -1;
    /** ระยะจากขอบบน weaFrame ถึงขอบบน weaInnerFrame (-1 = ชิดล่าง เว้นขอบล่างให้เอง) */
    public static final int WEA_INNER_TOP_PX = -1;
    /** ระยะจากขอบบน weaInnerFrame ถึงขอบบน weaShowBox (-1 = เท่ากับระยะขอบซ้ายขวา) */
    public static final int WEA_SHOWBOX_TOP_PX = -1;

    // =====================================================
    // ปัดป้อง (parry) — ปรับความยากตรงนี้
    // =====================================================
    /** ศัตรู "ง้าง" (มีสัญลักษณ์เตือนเหนือหัว) นานกี่ ms ก่อนโจมตีจริง */
    public static final long TELEGRAPH_MS = 600;
    /** คลิกขวาไม่เกินกี่ ms ก่อนการโจมตีถึงตัว จึงนับว่าปัดป้องสำเร็จ (ยิ่งมากยิ่งง่าย แต่ต้องไม่เกิน TELEGRAPH_MS) */
    public static final long PARRY_WINDOW_MS = 350;
    /** หลังคลิกปัดป้องแล้ว ต้องรออีกกี่ ms การคลิกครั้งใหม่ถึงจะนับเป็นการปัดป้อง (กันกดรัวเอาชนะ) */
    public static final long PARRY_COOLDOWN_MS = 500;
    /** ศัตรูที่ถูกปัดป้องสำเร็จมึนงงนานกี่ ms */
    public static final long STUN_MS = 5000;

    // =====================================================
    // Debug
    // =====================================================
    /** true = วาดพื้นที่ที่ศัตรูมองเห็น (เหลือง = ยังไม่เห็น, แดง = เห็นผู้เล่นแล้ว) ไว้ช่วยปรับค่า */
    public static final boolean DEBUG_SIGHT = false;
}