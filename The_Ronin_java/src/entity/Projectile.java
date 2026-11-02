package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import main.Assets;
import main.Camera;
import main.GameConfig;

/** ลูกธนู: เคลื่อนที่แบบ projectile motion (วิถีโค้งตามแรงโน้มถ่วง) */
public class Projectile {
    public double x, y;          // ตำแหน่งกึ่งกลางลูกธนู
    private double vx, vy;
    public final int damage;
    public final boolean fromPlayer;
    public final boolean buffed;     // ยิงตอนมียาพลังโจมตี → วาดทับสีแดง

    private final double startX;
    private final int maxDistance;

    public static final double SPEED = 14;      // ความเร็วตอนปล่อย (px/เฟรม)
    public static final double GRAVITY = 0.25;  // แรงโน้มถ่วงของลูกธนู (เบากว่าตัวละคร ให้โค้งสวย)

    public Projectile(double x, double y, double vx, double vy,
                      int damage, boolean fromPlayer, int maxDistance) {
        this(x, y, vx, vy, damage, fromPlayer, false, maxDistance);
    }

    public Projectile(double x, double y, double vx, double vy,
                      int damage, boolean fromPlayer, boolean buffed, int maxDistance) {
        this.buffed = buffed;
        this.x = x;
        this.y = y;
        this.startX = x;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.fromPlayer = fromPlayer;
        this.maxDistance = maxDistance;
    }

    /**
     * สร้างลูกธนูที่ "ยิงจาก (sx,sy) ให้วิถีโค้งผ่านจุด (tx,ty)"
     *
     * ที่มาของสูตร: วิถีกระสุนคือ  y = x·tanθ − g·x² / (2v²cos²θ)
     * แทน u = tanθ และ 1/cos²θ = 1+u² จะได้สมการกำลังสองของ u:
     *     a·u² − d·u + (h + a) = 0      โดย a = g·d² / (2v²)
     * d = ระยะแนวนอน, h = ความสูงของเป้าเหนือจุดยิง
     * ใช้รากตัวเล็ก (วิถีโค้งต่ำ ถึงเร็ว) ถ้าไกลเกินจนไม่มีคำตอบ ให้ยิงที่ 45° (ไกลสุด)
     */
    public static Projectile aimed(double sx, double sy, double tx, double ty,
                                   int damage, boolean fromPlayer, int maxDistance) {
        return aimed(sx, sy, tx, ty, damage, fromPlayer, false, maxDistance);
    }

    public static Projectile aimed(double sx, double sy, double tx, double ty,
                                   int damage, boolean fromPlayer, boolean buffed, int maxDistance) {
        double dx = tx - sx;
        double h = sy - ty;                 // แกน y ของจอชี้ลง จึงกลับเครื่องหมาย
        int dir = dx >= 0 ? 1 : -1;
        double d = Math.max(Math.abs(dx), 1);

        double v = SPEED, g = GRAVITY;
        double a = g * d * d / (2 * v * v);
        double disc = d * d - 4 * a * (h + a);

        // รูปแบบที่คงเสถียรของรากตัวเล็ก (หลีกเลี่ยงการลบเลขใกล้กัน)
        double u = disc >= 0 ? 2 * (h + a) / (d + Math.sqrt(disc)) : 1.0;
        double theta = Math.atan(u);

        return new Projectile(sx, sy,
                dir * v * Math.cos(theta), -v * Math.sin(theta),
                damage, fromPlayer, buffed, maxDistance);
    }

    public void update() {
        // เคลื่อนที่ด้วยความเร่งคงที่: y += vy + g/2 แล้วค่อยเพิ่ม vy (ตรงกับสูตรจริงแบบไม่ต่อเนื่อง)
        x += vx;
        y += vy + 0.5 * GRAVITY;
        vy += GRAVITY;
    }

    public boolean isExpired(int stageWidth, int groundY) {
        return x < 0 || x > stageWidth || y >= groundY
                || Math.abs(x - startX) > maxDistance;
    }

    private double angle() {
        return Math.atan2(vy, vx);
    }

    /** hitbox อยู่ที่ปลายหัวลูกธนู */
    public Rectangle getBounds() {
        double a = angle();
        int tipX = (int) (x + Math.cos(a) * GameConfig.ARROW_HALF_LENGTH);
        int tipY = (int) (y + Math.sin(a) * GameConfig.ARROW_HALF_LENGTH);
        int s = GameConfig.ARROW_TIP_SIZE;
        return new Rectangle(tipX - s / 2, tipY - s / 2, s, s);
    }

    public void draw(Graphics2D g2, Camera cam) {
        int sx = (int) x - cam.x;
        int sy = (int) y - cam.y;
        BufferedImage img = Assets.arrow(buffed);

        if (img == null) {
            g2.setColor(fromPlayer ? Color.yellow : Color.orange);
            g2.fillRect(sx - 12, sy - 3, 24, 6);
            return;
        }

        // หมุนรูปตามทิศที่ลูกธนูพุ่งไป (หัวธนูชี้ไปตามวิถี)
        int w = img.getWidth() * GameConfig.ARROW_SCALE, h = img.getHeight() * GameConfig.ARROW_SCALE;
        AffineTransform old = g2.getTransform();
        g2.translate(sx, sy);
        g2.rotate(angle());
        g2.drawImage(img, -w / 2, -h / 2, w, h, null);
        g2.setTransform(old);
    }
}