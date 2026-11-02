package entity;

public enum Weapon {
    //            ชื่อ       dmg  range cooldownMs deflect ranged heal
    KATANA   ("Katana",   30,  90,  400, true,  false, 0),
    NINJAKATANA   ("NinjaKatana",   15,  70,  300, true,  false, 0),
    YARI     ("Yari",     45, 150,  850, false, false, 0),
    NAGINATA ("Naginata", 25,  85,  600, true,  false, 5),
    YUMI     ("Yumi",     20, 700,  1200, false, true,  0);

    public final String name;
    public final int damage;
    public final int range;
    public final int cooldownMs;
    public final boolean deflectsArrows;
    public final boolean ranged;
    public final int healAmount;   // HP ที่ฟื้นทุก 3 วินาทีขณะถือ

    /**
     * อาวุธแบบ "แทง" — พุ่งไปข้างหน้าตอนโจมตี และใช้ effect แบบยืดตามระยะ (ทวน, นากินาตะ)
     * อาวุธที่ใช้ "ฟัน" (katana, ninjakatana) วาด slash อยู่กับที่
     * เพิ่มอาวุธใหม่: ถ้าไม่ได้ใส่ในลิสต์นี้ จะถือเป็นแบบฟันโดยอัตโนมัติ (ไม่ต้องไปแก้ที่ CharacterRenderer)
     */
    public boolean lunges() {
        return this == YARI || this == NAGINATA;
    }

    /**
     * ชื่อที่แสดงให้ผู้เล่นเห็น (เมนู, HUD, ป้ายบนพื้น)
     * ส่วน name ใช้เป็นชื่อไฟล์รูปด้วย (Assets โหลด name + ".png") จึงไม่เปลี่ยน
     */
    public String displayName() {
        return this == NINJAKATANA ? "Nindachi" : name;
    }

    Weapon(String name, int damage, int range, int cooldownMs,
           boolean deflectsArrows, boolean ranged, int healAmount) {
        this.name = name;
        this.damage = damage;
        this.range = range;
        this.cooldownMs = cooldownMs;
        this.deflectsArrows = deflectsArrows;
        this.ranged = ranged;
        this.healAmount = healAmount;
    }
}