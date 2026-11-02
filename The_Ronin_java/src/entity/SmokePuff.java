package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import main.Assets;
import main.Camera;
import main.GameConfig;

/**
 * ควันวาร์ป (smoke.png): ค่อยๆ ชัดขึ้นแล้วจางหายไป พร้อมหมุนไปเรื่อยๆ
 * ใช้ GamePanel.effects เก็บและวาด — อายุหมดแล้วถูกลบเอง
 */
public class SmokePuff implements Effect {
    private final double cx, cy;          // จุดกึ่งกลางในพิกัดโลก
    private final long start = System.currentTimeMillis();
    private final double startAngle = Math.random() * Math.PI * 2;
    private final double spin = (Math.random() < 0.5 ? -1 : 1) * Math.PI;   // หมุนครึ่งรอบต่อวินาที

    public SmokePuff(double cx, double cy) {
        this.cx = cx;
        this.cy = cy;
    }

    /** ความคืบหน้า 0 → 1 */
    private double progress() {
        return (System.currentTimeMillis() - start) / (double) GameConfig.SMOKE_MS;
    }

    @Override
    public boolean isFinished() {
        return progress() >= 1.0;
    }

    /**
     * fade in แล้ว fade out ด้วยเส้นโค้งเดียว: sin(π·t)
     * t=0 → 0 (ไม่เห็น), t=0.5 → 1 (ชัดสุด), t=1 → 0 (หายไป)
     */
    public static float fadeAlpha(double t) {
        t = Math.max(0, Math.min(1, t));
        return (float) Math.sin(Math.PI * t);
    }

    @Override
    public void draw(Graphics2D g2, Camera cam) {
        double t = progress();
        if (t >= 1.0) return;

        double angle = startAngle + spin * (System.currentTimeMillis() - start) / 1000.0;

        Composite oldC = g2.getComposite();
        AffineTransform oldT = g2.getTransform();

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha(t)));
        g2.translate(cx - cam.x, cy - cam.y);
        g2.rotate(angle);

        BufferedImage img = Assets.smoke();
        if (img != null) {
            int w = img.getWidth() * GameConfig.SMOKE_SCALE;
            int h = img.getHeight() * GameConfig.SMOKE_SCALE;
            g2.drawImage(img, -w / 2, -h / 2, w, h, null);
        } else {
            // ยังไม่มีรูป: วงกลมสีเทา 2 ก้อนซ้อนกันพอให้เห็นการหมุน
            int d = 16 * GameConfig.SCALE;
            g2.setColor(new Color(190, 190, 200));
            g2.fillOval(-d / 2, -d / 2, d, d);
            g2.setColor(new Color(140, 140, 155));
            g2.fillOval(-d / 4, -d / 2, d / 2, d / 2);
        }

        g2.setTransform(oldT);
        g2.setComposite(oldC);
    }
}