package entity;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ชนิดของไอเทมที่ดรอปจากศัตรู
 *
 * แต่ละค่าใน enum มีพฤติกรรมของตัวเอง (apply) — เป็น abstract method ที่ทุกค่าต้อง override
 * เพิ่มไอเทมใหม่ = เพิ่มค่าใน enum หนึ่งค่า ไม่ต้องไปแก้ if/else ที่อื่น
 */
public enum ItemType {

    //          ไฟล์รูป        ชื่อ              โอกาสดรอป   สีสำรอง (ใช้เมื่อยังไม่มีรูป)
    //                                          (ต่อศัตรู 1 ตัว)
    HP_POTION("HPbottle.png", "HP Potion",     0.10, new Color(220, 50, 50)) {
        @Override public void apply(Player p) { p.heal(HEAL_AMOUNT); }
        // เลือดเต็มแล้วไม่เก็บ ปล่อยไว้บนพื้นให้เก็บทีหลัง
        @Override public boolean canUse(Player p) { return p.getHP() < p.getMaxHP(); }
    },

    DAMAGE_POTION("DMGbottle.png", "Damage Potion", 0.05, new Color(240, 150, 30)) {
        @Override public void apply(Player p) { p.boostDamage(DAMAGE_BOOST_MS); }
    },

    ARMOUR("armour.png", "Armour",             0.05, new Color(150, 160, 180)) {
        @Override public void apply(Player p) { p.equipArmour(); }
        // เกราะเต็มแล้วไม่เก็บ (ถ้าสึกแล้วเก็บได้ = ซ่อมกลับเป็น 100)
        @Override public boolean canUse(Player p) { return p.getArmourHP() < ARMOUR_MAX_HP; }
    };

    // =====================================================
    // เรตการดรอป — ปรับตรงนี้
    //   dropRate ของแต่ละไอเทมคือโอกาสที่ศัตรู 1 ตัวจะดรอปไอเทมนั้น (0.10 = 10%)
    //   ศัตรู 1 ตัวดรอปไอเทมสุ่มได้อย่างมาก 1 ชิ้น จึงรวมกันต้องไม่เกิน 100%
    //   (ตอนนี้รวม 20%: ยาเลือด 10% + ยาพลัง 5% + เกราะ 5%)
    // =====================================================
    /** ตัวคูณเรตดรอปของทุกไอเทม: 1.0 = ตามตาราง, 5.0 = ดรอปบ่อยขึ้น 5 เท่า (ไว้ทดสอบ ตอนนี้รวม = 100%) */
    public static final double DROP_RATE_SCALE = 1.0;

    // =====================================================
    // ค่าบาลานซ์ของไอเทม — ปรับตรงนี้
    // =====================================================
    public static final int HEAL_AMOUNT = 50;                 // ยาเลือดฟื้นกี่ HP
    public static final int DAMAGE_MULTIPLIER = 2;            // ยาพลังโจมตีคูณกี่เท่า
    public static final long DAMAGE_BOOST_MS = 10_000;        // ออกฤทธิ์นานกี่ ms
    public static final double ARMOUR_REDUCTION = 0.35;       // เกราะลดดาเมจ 35% (100 → 65)
    public static final int ARMOUR_MAX_HP = 100;              // เกราะมี HP ของตัวเอง หมดแล้วหลุดจากตัว
    public static final double ARMOUR_WEAR_FACTOR = 1.0;      // ดาเมจที่เข้ามา (หลังบล็อก) ทำให้ HP เกราะลดกี่เท่า

    static {
        // กันตั้งเรตผิด: รวมเกิน 100% ไอเทมท้ายตารางจะไม่มีวันดรอป
        double total = 0;
        for (ItemType t : values()) total += t.dropRate;
        if (total * DROP_RATE_SCALE > 1.0 + 1e-9) {
            throw new IllegalStateException(
                "ผลรวมเรตดรอปเกิน 100%: " + Math.round(total * DROP_RATE_SCALE * 100) + "%");
        }
    }

    public final String fileName;
    public final String label;
    public final double dropRate;
    public final Color fallbackColor;

    ItemType(String fileName, String label, double dropRate, Color fallbackColor) {
        this.fileName = fileName;
        this.label = label;
        this.dropRate = dropRate;
        this.fallbackColor = fallbackColor;
    }

    /** ใช้ไอเทมกับผู้เล่น */
    public abstract void apply(Player p);

    /** ผู้เล่นเก็บได้ไหมตอนนี้ (ค่าเริ่มต้น: ได้เสมอ) */
    public boolean canUse(Player p) {
        return true;
    }

    /**
     * ศัตรูตัวหนึ่งตาย: สุ่มครั้งเดียวแล้วเทียบกับช่วงของแต่ละไอเทมต่อกัน
     * เช่น เรต 10/5/5% → 0–10% = ยาเลือด, 10–15% = ยาพลัง, 15–20% = เกราะ, เกินนั้น = ไม่ดรอป
     * คืน null = ไม่ดรอป
     */
    public static ItemType roll() {
        double r = ThreadLocalRandom.current().nextDouble();
        double acc = 0;
        for (ItemType t : values()) {
            acc += t.dropRate * DROP_RATE_SCALE;
            if (r < acc) return t;
        }
        return null;
    }
}