package entity;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;

import main.Assets;
import main.GamePanel;

/**
 * พระนักรบ — ตัวซัพพอร์ต
 *   + heal ศัตรูตัวอื่นที่อยู่รอบตัวในรัศมี HEAL_RADIUS
 *   − โจมตีช้ากว่าศัตรูอื่น 20% (ค่าตัวคูณ SLOWDOWN)
 *   ตายแล้วดรอปยาเลือด (HPbottle) แน่นอน
 */
public class WarriorMonk extends Enemy {

    // ---------- ค่าของความสามารถพิเศษ ----------
    private static final int HEAL_RADIUS = 200;          // รัศมี heal (px)
    private static final int HEAL_AMOUNT = 10;           // heal ต่อครั้ง (HP)
    private static final long HEAL_INTERVAL_MS = 2000;   // heal ทุกกี่ ms
    private static final double SLOWDOWN = 1.2;          // เวลาคูลดาวน์ × 1.2 = ช้าลง 20%
    private static final long RING_MS = 600;             // วงแหวนเขียวตอน heal แสดงนานแค่ไหน

    private long lastHeal = 0;
    private long ringStart = 0;

    public WarriorMonk(GamePanel gp, Player player, int x, Weapon weapon) {
        super(gp, player, x, weapon);

        sprite = Assets.warriorMonk;

        speed = 3;
        sightRange = 450;   // เห็นผู้เล่นไกลสุดกี่พิกเซล (ด้านหน้า)
        maxHP = 125;
        HP = maxHP;

        cooldownMultiplier = SLOWDOWN;
        addGuaranteedDrop(ItemType.HP_POTION);

        y = gp.groundY - height;
    }

    @Override
    public void update() {
        super.update();
        if (alive && !isStunned()) healAllies();   // มึนอยู่ = heal ไม่ได้
    }

    /**
     * ทุก HEAL_INTERVAL_MS: heal ศัตรูตัวอื่นที่เลือดไม่เต็มและอยู่ในรัศมี
     * ถ้าไม่มีใครเจ็บ จะไม่เริ่มนับเวลา — พอมีคนเจ็บก็ heal ได้ทันที
     */
    private void healAllies() {
        long now = System.currentTimeMillis();
        if (now - lastHeal < HEAL_INTERVAL_MS) return;

        boolean healedAny = false;
        for (Enemy other : gp.enemies) {
            if (other == this || !other.isAlive()) continue;
            double dx = other.getCenterX() - getCenterX();
            double dy = (other.getY() + other.getHeight() / 2.0) - (y + height / 2.0);
            if (Math.hypot(dx, dy) <= HEAL_RADIUS && other.heal(HEAL_AMOUNT)) {
                healedAny = true;
            }
        }
        if (healedAny) {
            lastHeal = now;
            ringStart = now;
        }
    }

    @Override
    public void draw(Graphics2D g2) {
        super.draw(g2);
        drawHealRing(g2);
    }

    /** วงแหวนสีเขียวขยายจากตัวพระออกไปถึงขอบรัศมี heal แล้วจางหาย */
    private void drawHealRing(Graphics2D g2) {
        long t = System.currentTimeMillis() - ringStart;
        if (t < 0 || t >= RING_MS) return;

        double k = t / (double) RING_MS;                       // 0 → 1
        int r = (int) (HEAL_RADIUS * (0.3 + 0.7 * k));
        int cx = (int) getCenterX() - gp.camera.x;
        int cy = (int) (y + height / 2.0) - gp.camera.y;

        Composite oldC = g2.getComposite();
        Stroke oldS = g2.getStroke();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (0.6 * (1 - k))));
        g2.setColor(new Color(90, 230, 120));
        g2.setStroke(new BasicStroke(6));
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);
        g2.setStroke(oldS);
        g2.setComposite(oldC);
    }
}