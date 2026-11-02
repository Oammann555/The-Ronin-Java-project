package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import main.Assets;
import main.GameConfig;
import main.GamePanel;
import main.KeyHandler;
import main.MouseHandler;

public class Player extends Entity {
    GamePanel gp;
    KeyHandler keyH;
    MouseHandler mouseH;

    double jumpPower = -12;
    boolean onGround = false;

    Weapon weapon = Weapon.KATANA;
    boolean blocking = false;

    long lastAttack = 0;
    long lastHeal = System.currentTimeMillis();

    // ---------- ผลจากไอเทม ----------
    int armourHP = 0;            // 0 = ไม่ได้ใส่เกราะ
    long damageBoostUntil = 0;   // เวลา (ms) ที่ยาพลังโจมตีหมดฤทธิ์

    /** ป้องกันด้วยเมาส์ขวาแล้วรับดาเมจกี่เท่า (0.1 = เหลือ 10%) */
    static final double BLOCK_DAMAGE_TAKEN = 0.1;

    public Player(GamePanel gp, KeyHandler keyH, MouseHandler mouseH) {
        this.gp = gp;
        this.keyH = keyH;
        this.mouseH = mouseH;
        setDefault();
    }

    public void setDefault() {
        x = 100;
        y = 200;
        speed = 4;
        width = GameConfig.HITBOX_W;
        height = GameConfig.HITBOX_H;
        maxHP = 100;
        HP = maxHP;
        alive = true;
        velocityY = 0;
        facing = 1;
        blocking = false;
        weapon = Weapon.NAGINATA;
        lastHeal = System.currentTimeMillis();
        armourHP = 0;
        damageBoostUntil = 0;
    }

    /** รีเซ็ตแค่ตำแหน่ง (ใช้ตอนเปลี่ยนด่าน เก็บ HP/อาวุธไว้) */
    public void setDefaultPositionOnly() {
        x = 100;
        y = 200;
        velocityY = 0;
    }

    public void update() {
        if (!alive) return;

        // ---------- เดิน ----------
        if (keyH.aPressed) { x -= speed; facing = -1; }
        if (keyH.dPressed) { x += speed; facing = 1; }

        if (x < 0) x = 0;
        if (x > gp.stageWidth - width) x = gp.stageWidth - width;

        // ---------- กระโดด ----------
        if (keyH.wPressed && onGround) {
            velocityY = jumpPower;
            onGround = false;
        }

        // ---------- แรงโน้มถ่วง ----------
        velocityY += gp.gravity;
        y += velocityY;

        if (y + height >= gp.groundY) {
            y = gp.groundY - height;
            velocityY = 0;
            onGround = true;
        }

        // ---------- ป้องกัน / โจมตี ----------
        blocking = mouseH.blocking;
        if (mouseH.leftClicked) {
            mouseH.leftClicked = false;
            tryAttack();
        }

        // ---------- Naginata: ฟื้นเลือด ----------
        long now = System.currentTimeMillis();
        if (weapon.healAmount > 0 && now - lastHeal >= 3000) {
            HP = Math.min(maxHP, HP + weapon.healAmount);
            lastHeal = now;
        }
    }

    private Rectangle meleeBox() {
        int bx = facing == 1 ? (int) x + width : (int) x - weapon.range;
        return new Rectangle(bx, (int) y, weapon.range, height);
    }

    private void tryAttack() {
        long now = System.currentTimeMillis();
        if (blocking || now - lastAttack < weapon.cooldownMs) return;
        lastAttack = now;
        swingStart = now;

        if (weapon.ranged) {
            shootArrow();
            return;
        }

        Rectangle hit = meleeBox();

        for (Enemy e : gp.enemies) {
            if (e.isAlive() && hit.intersects(e.getBounds())) {
                e.takeDamage(weapon.damage * getDamageMultiplier());
            }
        }

        // ฟันปัดธนู (katana / naginata)
        if (weapon.deflectsArrows) {
            gp.projectiles.removeIf(p -> !p.fromPlayer && hit.intersects(p.getBounds()));
        }
    }

    /** ยิงธนูให้วิถีโค้งผ่านตำแหน่งเมาส์ */
    private void shootArrow() {
        double targetX = mouseH.mouseX + gp.camera.x;
        double targetY = mouseH.mouseY + gp.camera.y;

        facing = targetX >= getCenterX() ? 1 : -1;   // หันไปทางที่เล็ง

        double startX = getCenterX() + facing * GameConfig.MUZZLE;
        double startY = y + GameConfig.HAND_Y;

        gp.projectiles.add(Projectile.aimed(
            startX, startY, targetX, targetY,
            weapon.damage * getDamageMultiplier(), true,
            getDamageMultiplier() > 1,          // ยิงตอนมียาพลัง → ลูกธนูทับสีแดง
            weapon.range));
    }

    /**
     * รับดาเมจ ตามลำดับ: บล็อก → เกราะ
     *  1) บล็อก (โดนจากด้านหน้า) เหลือ BLOCK_DAMAGE_TAKEN ของดาเมจ
     *  2) ถ้าใส่เกราะ: HP เกราะลดลงตามดาเมจที่เข้ามา (× ARMOUR_WEAR_FACTOR)
     *     และผู้เล่นรับเพียง (1 − ARMOUR_REDUCTION) ของดาเมจ — เช่น 100 → 65
     *     HP เกราะถึง 0 = เกราะหลุดจากตัว
     * ดาเมจที่โดนจริงขั้นต่ำ 1 เสมอ
     */
    public void takeDamage(int dmg, double sourceX) {
        double taken = dmg;
        boolean inFront = (sourceX - getCenterX()) * facing > 0;
        if (blocking && inFront) {
            taken *= BLOCK_DAMAGE_TAKEN;
        }
        if (armourHP > 0) {
            int wear = (int) Math.round(taken * ItemType.ARMOUR_WEAR_FACTOR);
            armourHP = Math.max(0, armourHP - wear);
            taken *= 1.0 - ItemType.ARMOUR_REDUCTION;
        }
        super.takeDamage(Math.max(1, (int) Math.round(taken)));
    }

    /**
     * ปัดป้อง: ผู้เล่นคลิกขวาไม่เกิน PARRY_WINDOW_MS ก่อนการโจมตีถึงตัว และผู้โจมตีอยู่ด้านที่หันหน้าไป
     * ถ้าสำเร็จ ผู้เรียกต้องไม่ทำดาเมจ (และศัตรูระยะประชิดจะมึน)
     * กดค้างไว้นานกว่านั้น หรือคลิกไม่ทัน ไม่นับ → ตกไปเป็นการป้องกันธรรมดาใน takeDamage (ถ้ากดค้างอยู่)
     */
    public boolean tryParry(double sourceX) {
        if (!alive) return false;
        boolean inFront = (sourceX - getCenterX()) * facing > 0;
        long age = System.currentTimeMillis() - mouseH.blockPressedAt;
        return inFront && age >= 0 && age <= GameConfig.PARRY_WINDOW_MS;
    }

    // ---------- ไอเทม (เรียกจาก ItemType.apply) ----------
    public void heal(int amount) {
        HP = Math.min(maxHP, HP + amount);
    }

    public void boostDamage(long durationMs) {
        damageBoostUntil = System.currentTimeMillis() + durationMs;   // กินซ้ำ = รีเซ็ตเวลา ไม่ทบ
    }

    /** ใส่เกราะ (หรือซ่อมเกราะที่ใส่อยู่) ให้ HP เกราะเต็ม */
    public void equipArmour() {
        armourHP = ItemType.ARMOUR_MAX_HP;
    }

    public boolean isArmoured() { return armourHP > 0; }

    public int getArmourHP() { return armourHP; }

    /** 2 ตอนยาออกฤทธิ์ ไม่งั้น 1 */
    public int getDamageMultiplier() {
        return damageBoostRemainingMs() > 0 ? ItemType.DAMAGE_MULTIPLIER : 1;
    }

    public long damageBoostRemainingMs() {
        return Math.max(0, damageBoostUntil - System.currentTimeMillis());
    }

    public Weapon getWeapon() { return weapon; }

    /** สลับอาวุธ คืนค่าอาวุธเดิม */
    public Weapon swapWeapon(Weapon next) {
        Weapon old = weapon;
        weapon = next;
        lastHeal = System.currentTimeMillis();
        return old;
    }

    public void draw(Graphics2D g2) {
        CharacterRenderer.draw(g2, this, gp.camera.x, gp.camera.y,
                Assets.player, Color.white, weapon, swingStart,
                isArmoured() ? Assets.armourWorn() : null,
                getDamageMultiplier() > 1);

        // โล่ตอนบล็อก (ยังไม่มีรูป จึงใช้แถบสีฟ้าไปก่อน)
        if (blocking) {
            g2.setColor(Color.cyan);
            int sx = (int) x - gp.camera.x;
            int sy = (int) y - gp.camera.y;
            int shieldX = facing == 1 ? sx + width : sx - 6;
            g2.fillRect(shieldX, sy, 6, height);
        }
    }
}