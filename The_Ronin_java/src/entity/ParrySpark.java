package entity;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;

import main.Camera;
import main.GameConfig;

/** ประกายวงแหวนกับข้อความ PARRY! ตอนปัดป้องสำเร็จ */
public class ParrySpark implements Effect {
    private static final long LIFE_MS = 550;

    private final double x, y;
    private final long start = System.currentTimeMillis();

    public ParrySpark(double x, double y) {
        this.x = x;
        this.y = y;
    }

    private double progress() {
        return (System.currentTimeMillis() - start) / (double) LIFE_MS;
    }

    @Override
    public boolean isFinished() {
        return progress() >= 1.0;
    }

    @Override
    public void draw(Graphics2D g2, Camera cam) {
        double t = progress();
        if (t >= 1.0) return;

        int cx = (int) x - cam.x, cy = (int) y - cam.y;
        float alpha = (float) (1.0 - t);
        int r = (int) ((10 + 14 * t * GameConfig.SCALE) );

        Composite oldC = g2.getComposite();
        Stroke oldS = g2.getStroke();
        Font oldF = g2.getFont();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // วงแหวนขยายออก
        g2.setColor(new Color(170, 235, 255));
        g2.setStroke(new BasicStroke(5));
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);

        // ข้อความลอยขึ้น
        g2.setFont(new Font("Arial", Font.BOLD, 30));
        FontMetrics fm = g2.getFontMetrics();
        String text = "PARRY!";
        int ty = cy - 30 - (int) (40 * t);
        g2.setColor(Color.black);
        g2.drawString(text, cx - fm.stringWidth(text) / 2 + 2, ty + 2);
        g2.setColor(new Color(200, 245, 255));
        g2.drawString(text, cx - fm.stringWidth(text) / 2, ty);

        g2.setFont(oldF);
        g2.setStroke(oldS);
        g2.setComposite(oldC);
    }
}
