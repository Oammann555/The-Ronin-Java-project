package entity;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import main.GameConfig;
import main.GamePanel;

public class Enemy extends Entity {
    GamePanel gp;
    Player player;
    Weapon weapon;

    /** subclass กำหนดรูปของตัวเอง (Ashigaru, Ninja, ...) */
    protected BufferedImage sprite;
    protected Color fallbackColor = Color.red;   // ใช้เมื่อโหลดรูปไม่ได้

    long lastAttack;

    /** ไอเทมที่ศัตรูตัวนี้ดรอปแน่นอนเมื่อตาย (ถ้าลิสต์ว่าง = สุ่มตาม ItemType.roll()) */
    private final List<ItemType> guaranteedDrops = new ArrayList<>();

    /** ตัวคูณเวลาคูลดาวน์การโจมตี: 1.0 = ปกติ, 1.2 = ช้าลง 20% (subclass ปรับได้) */
    protected double cooldownMultiplier = 1.0;

    private long healedAt = 0;                           // เวลาที่ถูก heal ล่าสุด (ไว้แสดงเครื่องหมาย +)
    private static final long HEAL_MARK_MS = 700;

    // =====================================================
    // ระยะมองเห็น
    //   - เห็นผู้เล่นในทิศที่หันหน้าไกลเต็ม sightRange
    //   - เห็นด้านหลังแค่ REAR_SIGHT_FACTOR ของระยะนั้น (เหมือนได้ยินเสียงฝีเท้า)
    //   - เห็นแล้วจะไล่ตาม จนกว่าผู้เล่นจะไกลเกิน sightRange × LOSE_SIGHT_FACTOR จึงเลิกไล่
    //     (ระยะเลิกไล่ > ระยะเริ่มเห็น กันศัตรูสลับไปมาตรงขอบ)
    //   - ถูกโจมตีเมื่อไรก็รู้ตัวทันทีไม่ว่าอยู่ไกลแค่ไหน
    // =====================================================
    protected int sightRange = 450;
    protected static final double REAR_SIGHT_FACTOR = 0.3;
    protected static final double LOSE_SIGHT_FACTOR = 1.5;
    protected static final long REACTION_MS = 500;       // หน่วงก่อนตีครั้งแรกหลังเห็น
    private static final long ALERT_MARK_MS = 1000;      // แสดงเครื่องหมาย ! นานแค่ไหน

    protected boolean alerted = false;
    private long alertedAt = 0;

    // =====================================================
    // โจมตีแบบมีสัญลักษณ์เตือน + ถูกปัดป้อง
    //   ศัตรู "ง้าง" นาน TELEGRAPH_MS (มีสัญลักษณ์ขึ้นเหนือหัว) แล้วค่อยโจมตีจริง
    //   ผู้เล่นคลิกขวาทันก่อนโจมตีถึงตัว = ปัดป้องสำเร็จ → ศัตรูมึน STUN_MS (ดูค่าใน GameConfig)
    // =====================================================
    private long windupStart = -1;     // เวลาที่เริ่มง้าง (-1 = ไม่ได้ง้างอยู่)
    private long stunnedUntil = 0;     // เวลาที่หายมึน

    // ลาดตระเวนไปมารอบจุดเกิดตอนที่ยังไม่เห็นผู้เล่น
    private final double homeX;
    protected int patrolRadius = 150;
    private int patrolDir = 1;

    public Enemy(GamePanel gp, Player player, int x, Weapon weapon) {
        this.gp = gp;
        this.player = player;
        this.weapon = weapon;
        this.x = x;
        this.homeX = x;
        this.width = GameConfig.HITBOX_W;
        this.height = GameConfig.HITBOX_H;
        this.lastAttack = System.currentTimeMillis();  // หน่วงก่อนตีครั้งแรก
    }

    public Weapon getWeapon() { return weapon; }
    public boolean isAlerted() { return alerted; }
    public List<ItemType> getGuaranteedDrops() { return guaranteedDrops; }

    public void addGuaranteedDrop(ItemType type) {
        if (!guaranteedDrops.contains(type)) guaranteedDrops.add(type);
    }

    /** heal ศัตรูตัวนี้ (ไม่เกินเลือดเต็ม) คืน true ถ้าเลือดเพิ่มขึ้นจริง */
    public boolean heal(int amount) {
        if (!alive || HP >= maxHP) return false;
        HP = Math.min(maxHP, HP + amount);
        healedAt = System.currentTimeMillis();
        return true;
    }

    /** เวลาที่ต้องรอระหว่างการโจมตีแต่ละครั้ง = ช้ากว่าผู้เล่น 2 เท่า × ตัวคูณของศัตรูตัวนั้น */
    protected long attackCooldownMs() {
        return Math.round(weapon.cooldownMs * 2L * cooldownMultiplier);
    }

    /** ศัตรูธนูต้องเห็นไกลอย่างน้อยเท่าระยะยิง ไม่งั้นจะยิงใส่คนที่ตัวเองยังไม่เห็น */
    private int effectiveSight() {
        return weapon.ranged ? Math.max(sightRange, weapon.range) : sightRange;
    }

    // ---------------------------------------------------------
    // Update
    // ---------------------------------------------------------
    public void update() {
        if (!alive) return;

        if (!isStunned()) {               // มึนอยู่ = ยืนนิ่ง (แรงโน้มถ่วงยังทำงาน)
            updateVision();
            if (alerted) chase();
            else patrol();
        }

        // แรงโน้มถ่วง
        velocityY += gp.gravity;
        y += velocityY;
        if (y + height >= gp.groundY) {
            y = gp.groundY - height;
            velocityY = 0;
        }

        if (x < 0) x = 0;
        if (x > gp.stageWidth - width) x = gp.stageWidth - width;
    }

    private void updateVision() {
        if (!player.isAlive()) {
            alerted = false;
            cancelWindup();
            return;
        }
        double dx = player.getCenterX() - getCenterX();
        double dist = Math.abs(dx);
        double sight = effectiveSight();

        boolean inFront = dx * facing > 0;
        double range = inFront ? sight : sight * REAR_SIGHT_FACTOR;

        if (dist <= range) {
            alert();
        } else if (alerted && dist > sight * LOSE_SIGHT_FACTOR) {
            alerted = false;
            cancelWindup();
        }
    }

    /** รู้ตัวว่ามีผู้เล่น (เห็นเอง หรือถูกโจมตี) */
    protected void alert() {
        if (alerted) return;
        alerted = true;
        long now = System.currentTimeMillis();
        alertedAt = now;

        // เวลาตอบสนอง: ยังไม่ให้ตีทันที ต้องรออีก REACTION_MS
        long earliest = now + REACTION_MS - attackCooldownMs();
        if (lastAttack < earliest) lastAttack = earliest;
    }

    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        if (alive) alert();
    }

    private void patrol() {
        facing = patrolDir;
        x += patrolDir * (speed / 2.0);   // เดินช้ากว่าตอนไล่
        if (x > homeX + patrolRadius) patrolDir = -1;
        else if (x < homeX - patrolRadius) patrolDir = 1;
    }

    private void chase() {
        double dx = player.getCenterX() - getCenterX();
        double dist = Math.abs(dx);

        // ตอนง้างอยู่: ทิศและตำแหน่งล็อก (ผู้เล่นหลบได้ด้วยการถอยออกนอกระยะหรือกระโดดข้าม)
        if (!isWindingUp()) {
            facing = dx > 0 ? 1 : -1;

            // ยืนในระยะตามอาวุธ (ธนูยืนไกลกว่า)
            double preferred = weapon.ranged ? weapon.range * 0.6 : weapon.range * 0.8;
            if (dist > preferred) x += facing * speed;
        }

        attack();
    }

    // ---------------------------------------------------------
    // Attack
    // ---------------------------------------------------------
    private Rectangle meleeBox() {
        int bx = facing == 1 ? (int) x + width : (int) x - weapon.range;
        return new Rectangle(bx, (int) y, weapon.range, height);
    }

    public boolean isStunned() { return System.currentTimeMillis() < stunnedUntil; }
    public boolean isWindingUp() { return windupStart >= 0; }

    /** ยกเลิกการง้าง (เสียเป้าหมาย, ถูกสตัน, วาร์ป ...) */
    protected void cancelWindup() { windupStart = -1; }

    /** มึนงง: ยกเลิกการโจมตีที่ง้างอยู่ และเริ่มนับคูลดาวน์ใหม่ตั้งแต่ตอนหายมึน */
    protected void stun(long ms) {
        stunnedUntil = System.currentTimeMillis() + ms;
        cancelWindup();
        lastAttack = stunnedUntil;
    }

    /**
     * เรียกทุกเฟรมตอนไล่ล่า: ง้าง → (ครบ TELEGRAPH_MS) → โจมตีจริง
     * เริ่มง้างเมื่อพร้อมและเป้าหมายอยู่ในระยะ (หักเวลาง้างออกจากคูลดาวน์ จังหวะโจมตีโดยรวมจึงเท่าเดิม)
     */
    public void attack() {
        if (isStunned()) return;
        long now = System.currentTimeMillis();

        if (isWindingUp()) {
            if (now - windupStart >= GameConfig.TELEGRAPH_MS) {
                windupStart = -1;
                strike(now);
            }
            return;
        }

        if (now - lastAttack < attackCooldownMs() - GameConfig.TELEGRAPH_MS) return;   // ช้ากว่าผู้เล่น
        boolean inRange = weapon.ranged
                ? Math.abs(player.getCenterX() - getCenterX()) <= weapon.range
                : meleeBox().intersects(player.getBounds());
        if (inRange) windupStart = now;
    }

    /** จังหวะโจมตีจริง (หลังง้างครบ) */
    private void strike(long now) {
        lastAttack = now;
        swingStart = now;

        if (weapon.ranged) {
            double startX = getCenterX() + facing * GameConfig.MUZZLE;
            double startY = y + GameConfig.HAND_Y;
            // เล็งที่กลางลำตัวผู้เล่น ณ ตอนปล่อย (ถ้าผู้เล่นวิ่งอยู่ ธนูมักพลาด)
            gp.projectiles.add(Projectile.aimed(
                startX, startY,
                player.getCenterX(), player.getY() + player.getHeight() / 2.0,
                weapon.damage, false, weapon.range + 100));
            return;
        }

        // ผู้เล่นหลบออกนอกระยะระหว่างที่ง้าง → ฟันพลาด
        if (!meleeBox().intersects(player.getBounds())) return;

        if (player.tryParry(getCenterX())) {
            // ปัดป้องสำเร็จ: ไม่ทำดาเมจ ศัตรูมึน
            stun(GameConfig.STUN_MS);
            gp.effects.add(new ParrySpark((getCenterX() + player.getCenterX()) / 2, y + GameConfig.HAND_Y));
        } else {
            player.takeDamage(weapon.damage, getCenterX());   // ไม่ทัน: ถ้ากดป้องกันค้างอยู่ก็แค่ลดดาเมจ
        }
    }

    // ---------------------------------------------------------
    // Draw
    // ---------------------------------------------------------
    protected void drawHealthBar(Graphics2D g2) {
        int barW = GameConfig.HEALTHBAR_W;
        int sx = (int) getCenterX() - gp.camera.x - barW / 2;
        int sy = (int) y - gp.camera.y - 12;
        g2.setColor(Color.darkGray);
        g2.fillRect(sx, sy, barW, 6);
        g2.setColor(Color.green);
        g2.fillRect(sx, sy, barW * HP / maxHP, 6);
        g2.setColor(Color.white);
        g2.drawString(weapon.displayName(), sx, sy - 4);

        // เครื่องหมาย ! ช่วงสั้นๆ หลังเห็นผู้เล่น
        if (alerted && System.currentTimeMillis() - alertedAt < ALERT_MARK_MS) {
            Font old = g2.getFont();
            g2.setFont(new Font("Arial", Font.BOLD, 32));
            g2.setColor(Color.red);
            g2.drawString("!", sx + barW / 2 - 5, sy - 22);
            g2.setFont(old);
        }

        // เครื่องหมาย + สีเขียว ช่วงสั้นๆ หลังถูก heal
        if (System.currentTimeMillis() - healedAt < HEAL_MARK_MS) {
            Font old = g2.getFont();
            g2.setFont(new Font("Arial", Font.BOLD, 32));
            g2.setColor(new Color(90, 230, 120));
            g2.drawString("+", sx + barW - 16, sy - 22);
            g2.setFont(old);
        }
    }

    private void drawSightDebug(Graphics2D g2) {
        double front = effectiveSight();
        double rear = front * REAR_SIGHT_FACTOR;
        double cx = getCenterX() - gp.camera.x;
        int left = (int) (facing == 1 ? cx - rear : cx - front);
        g2.setColor(alerted ? new Color(255, 0, 0, 40) : new Color(255, 255, 0, 40));
        g2.fillRect(left, (int) y - gp.camera.y, (int) (front + rear), height);
    }

    public void draw(Graphics2D g2) {
        if (GameConfig.DEBUG_SIGHT) drawSightDebug(g2);
        CharacterRenderer.draw(g2, this, gp.camera.x, gp.camera.y,
                sprite, fallbackColor, weapon, swingStart, null, false);
        drawHealthBar(g2);
        drawTelegraph(g2);
        drawStun(g2);
    }

    /**
     * สัญลักษณ์เตือนก่อนโจมตี: สามเหลี่ยมเหลือง = กำลังง้าง (ยังเร็วไป)
     * เปลี่ยนเป็นสีแดง = ถึงช่วงที่คลิกขวาแล้วปัดป้องได้ (PARRY_WINDOW_MS สุดท้ายก่อนโจมตีถึงตัว)
     */
    private void drawTelegraph(Graphics2D g2) {
        if (!isWindingUp()) return;
        long t = System.currentTimeMillis() - windupStart;
        boolean parryOpen = t >= GameConfig.TELEGRAPH_MS - GameConfig.PARRY_WINDOW_MS;

        double pulse = 1.0 + 0.12 * Math.sin(t / 60.0);
        int w = (int) (44 * pulse), h = (int) (40 * pulse);
        int cx = (int) getCenterX() - gp.camera.x;
        int top = (int) y - gp.camera.y - 78;                 // เหนือหลอดเลือดและเครื่องหมายอื่น

        Polygon tri = new Polygon(new int[] { cx, cx - w / 2, cx + w / 2 },
                                  new int[] { top, top + h, top + h }, 3);
        Stroke oldS = g2.getStroke();
        Font oldF = g2.getFont();
        g2.setColor(parryOpen ? new Color(235, 50, 40) : new Color(250, 205, 40));
        g2.fillPolygon(tri);
        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(3));
        g2.drawPolygon(tri);

        g2.setFont(new Font("Arial", Font.BOLD, (int) (h * 0.62)));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString("!", cx - fm.stringWidth("!") / 2, top + h - 6);
        g2.setFont(oldF);
        g2.setStroke(oldS);
    }

    /** ตอนมึน: ดาวหมุนเหนือหัว + แถบเวลามึนที่เหลือ */
    private void drawStun(Graphics2D g2) {
        if (!isStunned()) return;
        long now = System.currentTimeMillis();
        int cx = (int) getCenterX() - gp.camera.x;
        int headY = (int) y - gp.camera.y;

        for (int i = 0; i < 3; i++) {
            double a = now / 250.0 + i * Math.PI * 2 / 3;
            fillStar(g2, cx + (int) (Math.cos(a) * 32), headY + 6 + (int) (Math.sin(a) * 8), 9);
        }

        int barW = GameConfig.HEALTHBAR_W;
        int left = (int) (stunnedUntil - now);
        g2.setColor(Color.darkGray);
        g2.fillRect(cx - barW / 2, headY - 4, barW, 4);
        g2.setColor(new Color(255, 225, 60));
        g2.fillRect(cx - barW / 2, headY - 4, (int) (barW * Math.max(0, left) / (double) GameConfig.STUN_MS), 4);
    }

    private static void fillStar(Graphics2D g2, int cx, int cy, int r) {
        int[] xs = new int[10], ys = new int[10];
        for (int i = 0; i < 10; i++) {
            double ang = -Math.PI / 2 + i * Math.PI / 5;
            double rad = (i % 2 == 0) ? r : r * 0.45;
            xs[i] = cx + (int) Math.round(Math.cos(ang) * rad);
            ys[i] = cy + (int) Math.round(Math.sin(ang) * rad);
        }
        g2.setColor(new Color(255, 225, 60));
        g2.fillPolygon(xs, ys, 10);
    }
}