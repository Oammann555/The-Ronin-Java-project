package main;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import entity.ItemType;
import entity.Weapon;

/** โหลดรูปทั้งหมดครั้งเดียว แล้วให้ส่วนอื่นของเกมมาขอไปใช้ */
public final class Assets {
    private Assets() { }

    /** โฟลเดอร์ที่เก็บรูป (ตั้งไว้ที่เดียว) */
    private static final String DIR = "res/";

    // ตัวละคร (32x32 ทั้งหมด)
    public static BufferedImage player, ashigaru, ninja, samurai, warriorMonk;

    // อาวุธ: heldSprites = ภาพเต็ม 32x32 ไว้ทับตัวละคร, icons = ภาพที่ตัดขอบโปร่งใสออก ไว้วางบนพื้น
    private static final Map<Weapon, BufferedImage> heldSprites = new EnumMap<>(Weapon.class);
    private static final Map<Weapon, BufferedImage> icons = new EnumMap<>(Weapon.class);

    // ไอเทม (ยา/เกราะ) ตัดขอบโปร่งใสแล้ว — ถ้ายังไม่มีไฟล์จะเป็น null แล้ววาดสี่เหลี่ยมสีแทน
    private static final Map<ItemType, BufferedImage> itemIcons = new EnumMap<>(ItemType.class);

    // effect และลูกธนู ตัดขอบโปร่งใสออกแล้ว
    private static BufferedImage slashEffect, stabEffect, arrow;

    // เวอร์ชันทับสีแดง 50% ไว้ใช้ตอนผู้เล่นมียาพลังโจมตี (สร้างครั้งเดียวตอนโหลด)
    private static BufferedImage slashEffectRed, stabEffectRed, arrowRed;

    // ควันวาร์ป และชุดเกราะตัวเต็ม 32x32 ที่สวมทับผู้เล่น
    private static BufferedImage smoke, armourWorn;

    // ---------- งานศิลป์หน้าเมนู ----------
    // ไม่บังคับ: ถ้าไฟล์ไหนยังไม่มี (เป็น null) หน้าเมนูจะวาดส่วนนั้นด้วยโค้ดแทน
    public static BufferedImage menuScreen, startButton, exitButton, backButton;
    public static BufferedImage weaBackground, weaFrame, weaInnerFrame, weaShowBox;
    private static final Map<Weapon, BufferedImage> weaImages = new EnumMap<>(Weapon.class);
    private static final Map<BufferedImage, BufferedImage> brightCache = new IdentityHashMap<>();
    private static final List<String> missingUi = new ArrayList<>();

    private static boolean loaded = false;

    public static void load() {
        if (loaded) return;
        loaded = true;

        player      = read("Player.png");
        ashigaru    = read("Ashigaru.png");
        ninja       = read("Ninja.png");
        samurai     = read("Samurai.png");
        warriorMonk = read("warrior_monk.png");

        for (Weapon w : Weapon.values()) {
            BufferedImage img = read(w.name + ".png");   // Katana.png, Yari.png, ...
            heldSprites.put(w, img);
            icons.put(w, crop(img));
        }

        for (ItemType t : ItemType.values()) {
            BufferedImage img = read(t.fileName);       // HPbottle.png, DMGbottle.png, armour.png
            itemIcons.put(t, crop(img));
            if (t == ItemType.ARMOUR) armourWorn = img; // ภาพเต็ม 32x32 ไว้สวมทับตัวผู้เล่น
        }

        slashEffect = crop(read("Slash_effect.png"));
        stabEffect  = crop(read("Stap_effect.png"));
        arrow       = crop(read("arrow.png"));
        smoke       = crop(read("smoke.png"));

        slashEffectRed = tintRed(slashEffect);
        stabEffectRed  = tintRed(stabEffect);
        arrowRed       = tintRed(arrow);

        // งานศิลป์เมนู — ชื่อไฟล์ตามที่ตกลงกัน (ตัวพิมพ์เล็ก/ใหญ่ต้องตรงเป๊ะ)
        menuScreen    = readUi("Menuscreen.png", true);
        startButton   = readUi("startbutton.png", true);
        exitButton    = readUi("exitbutton.png", true);
        backButton    = readUi("backbutton.png", false);    // ไม่บังคับ: ไม่มีก็ใช้ปุ่มที่โค้ดวาด
        weaBackground = readUi("weaBackground.png", true);
        weaFrame      = readUi("weaFrame.png", true);
        weaInnerFrame = readUi("weaInnerFrame.png", true);
        weaShowBox    = readUi("weaShowBox.png", true);
        for (Weapon w : Weapon.values()) {
            // weaKatana.png, weaYari.png, weaNaginata.png, weaYumi.png, weaNindachi.png (ใช้ชื่อที่แสดง ไม่ใช่ชื่อไฟล์เกม)
            weaImages.put(w, readUi("wea" + w.displayName() + ".png", true));
        }
        if (!missingUi.isEmpty()) {
            System.out.println("[UI] ไม่พบรูป (ใช้ภาพที่โค้ดวาดแทนไปก่อน): " + String.join(", ", missingUi));
        }
    }

    // ---------- getter ----------
    public static BufferedImage heldSprite(Weapon w) { return heldSprites.get(w); }
    public static BufferedImage weaponIcon(Weapon w) { return icons.get(w); }
    public static BufferedImage itemIcon(ItemType t) { return itemIcons.get(t); }
    public static BufferedImage arrow() { return arrow; }
    public static BufferedImage weaImage(Weapon w) { return weaImages.get(w); }
    public static BufferedImage arrow(boolean buffed) { return buffed ? arrowRed : arrow; }
    public static BufferedImage smoke() { return smoke; }
    public static BufferedImage armourWorn() { return armourWorn; }

    /** katana → slash, yari/naginata → stab, yumi → ไม่มี */
    public static BufferedImage effect(Weapon w) {
        return effect(w, false);
    }

    /** buffed = true → ได้ effect เวอร์ชันทับสีแดง (ตอนมียาพลังโจมตี) */
    public static BufferedImage effect(Weapon w, boolean buffed) {
        return switch (w) {
            case KATANA, NINJAKATANA -> buffed ? slashEffectRed : slashEffect;
            case YARI, NAGINATA -> buffed ? stabEffectRed : stabEffect;
            case YUMI -> null;
        };
    }

    // ---------- เครื่องมือช่วยวาด ----------
    /** วาดภาพ ถ้า flip = true จะกลับซ้าย-ขวา (ใช้ตอนหันซ้าย) */
    public static void draw(Graphics2D g2, BufferedImage img, int x, int y, int w, int h, boolean flip) {
        if (img == null) return;
        if (flip) g2.drawImage(img, x + w, y, -w, h, null);
        else      g2.drawImage(img, x, y, w, h, null);
    }

    /**
     * ภาพเดียวกันแต่สว่างขึ้น ใช้เป็นสถานะ hover ของปุ่ม/การ์ด (สร้างครั้งเดียวแล้วจำไว้)
     * synchronized เพราะถูกเรียกจากทั้ง game thread (สร้างหน้าเมนู) และ EDT (วาด)
     */
    public static synchronized BufferedImage brightened(BufferedImage src) {
        if (src == null) return null;
        return brightCache.computeIfAbsent(src, Assets::makeBrighter);
    }

    private static BufferedImage makeBrighter(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int al = argb >>> 24;
                if (al == 0) continue;
                int r = Math.min(255, Math.round(((argb >> 16) & 0xFF) * 1.3f + 25));
                int g = Math.min(255, Math.round(((argb >> 8) & 0xFF) * 1.3f + 25));
                int b = Math.min(255, Math.round((argb & 0xFF) * 1.3f + 25));
                out.setRGB(x, y, (al << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return out;
    }

    // ---------- ภายใน ----------
    /** โหลดรูปเมนู: ไม่เจอไฟล์ไม่ถือเป็น error (คืน null) แค่จดชื่อไว้แจ้งครั้งเดียวตอนจบ */
    private static BufferedImage readUi(String name, boolean report) {
        try {
            return ImageIO.read(new File(DIR + name));
        } catch (IOException | IllegalArgumentException e) {
            if (report) missingUi.add(name);
            return null;
        }
    }

    private static BufferedImage read(String name) {
        try {
            return ImageIO.read(new File(DIR + name));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Cannot load image: " + DIR + name);
            return null;   // โค้ดวาดทุกที่ต้องรับมือกับ null (วาดสี่เหลี่ยมแทน)
        }
    }

    /**
     * ทับสีแดงบนภาพ: ผสมสีเดิมกับแดงล้วน (255,0,0) ตามสัดส่วน BUFF_TINT (0.5 = สีเดิม 50% + แดง 50%)
     * ความโปร่งใสของแต่ละพิกเซลคงเดิม ส่วนที่โปร่งใสอยู่แล้วจึงไม่ถูกย้อม
     */
    private static BufferedImage tintRed(BufferedImage src) {
        if (src == null) return null;
        float k = GameConfig.BUFF_TINT;
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int a = argb >>> 24;
                if (a == 0) continue;
                int r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
                r = Math.round(r * (1 - k) + 255 * k);
                g = Math.round(g * (1 - k));
                b = Math.round(b * (1 - k));
                out.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return out;
    }

    /** ตัดพื้นที่โปร่งใสรอบๆ ออก เหลือเฉพาะส่วนที่มีสี */
    private static BufferedImage crop(BufferedImage img) {
        if (img == null) return null;
        int minX = img.getWidth(), minY = img.getHeight(), maxX = -1, maxY = -1;
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                if ((img.getRGB(x, y) >>> 24) != 0) {
                    if (x < minX) minX = x;
                    if (y < minY) minY = y;
                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                }
            }
        }
        if (maxX < 0) return img;   // ภาพโปร่งใสทั้งหมด
        return img.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}