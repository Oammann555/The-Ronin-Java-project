package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import main.Assets;
import main.Camera;
import main.GameConfig;

/** ไอเทมที่ตกอยู่บนพื้น (เดินทับเพื่อเก็บ ไม่ต้องกดปุ่ม) */
public class ItemDrop {
    public final ItemType type;
    private final int x, y;
    private static final int W = GameConfig.ITEM_W, H = GameConfig.ITEM_H;

    public ItemDrop(int x, int y, ItemType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, W, H);
    }

    public void draw(Graphics2D g2, Camera cam) {
        int bx = x - cam.x;
        int by = y - cam.y;
        int labelY = by - 4;

        BufferedImage icon = Assets.itemIcon(type);
        if (icon != null) {
            int w = icon.getWidth() * GameConfig.SCALE;
            int h = icon.getHeight() * GameConfig.SCALE;
            int top = by + H - h;                      // ชิดพื้น กึ่งกลางตามแนวนอน
            g2.drawImage(icon, bx + W / 2 - w / 2, top, w, h, null);
            labelY = top - 4;
        } else {
            // ยังไม่มีรูป: วาดสี่เหลี่ยมสีประจำไอเทมแทน
            g2.setColor(type.fallbackColor);
            g2.fillRect(bx, by, W, H);
        }

        g2.setColor(Color.white);
        g2.drawString(type.label, bx, labelY);
    }
}
