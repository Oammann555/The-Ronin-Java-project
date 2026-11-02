package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * ปุ่มบนหน้าเมนู: รู้ว่าเมาส์อยู่บนปุ่มไหม และวาดตัวเองตามสถานะ
 * ถ้ามีรูปที่วาดเอง (skin) จะใช้รูปนั้น (ขนาดปุ่ม = ขนาดรูป × scale, ตอน hover ใช้สำเนาที่สว่างขึ้น)
 * ไม่งั้นวาดปุ่มสำรองด้วยโค้ด
 */
final class MenuButton {
    final Rectangle bounds;
    final String label;
    private final BufferedImage skin;
    boolean hovered;

    /** ปุ่มวาดด้วยโค้ดล้วน cx = กึ่งกลางแนวนอน, y = ขอบบน */
    MenuButton(String label, int cx, int y, int w, int h) {
        this(label, null, 1f, cx, y, w, h);
    }

    /** ปุ่มที่มีรูป: ถ้า skin เป็น null จะใช้ fallbackW x fallbackH และวาดด้วยโค้ดแทน */
    MenuButton(String label, BufferedImage skin, float scale, int cx, int y, int fallbackW, int fallbackH) {
        this.label = label;
        this.skin = skin;
        int w = skin != null ? Math.round(skin.getWidth() * scale) : fallbackW;
        int h = skin != null ? Math.round(skin.getHeight() * scale) : fallbackH;
        this.bounds = new Rectangle(cx - w / 2, y, w, h);
    }

    void update(int mouseX, int mouseY) {
        hovered = bounds.contains(mouseX, mouseY);
    }

    void draw(Graphics2D g2) {
        if (skin != null) {
            g2.drawImage(hovered ? Assets.brightened(skin) : skin,
                         bounds.x, bounds.y, bounds.width, bounds.height, null);
            return;
        }

        g2.setColor(hovered ? Ui.PANEL_HOVER : Ui.PANEL);
        g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 16, 16);
        g2.setColor(hovered ? Ui.GOLD : Ui.GOLD_DARK);
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 16, 16);

        g2.setFont(new Font("Arial", Font.BOLD, 28));
        FontMetrics fm = g2.getFontMetrics();
        int baseline = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
        Ui.centered(g2, label, bounds.x + bounds.width / 2, baseline,
                    hovered ? Color.white : new Color(215, 215, 220));
    }
}