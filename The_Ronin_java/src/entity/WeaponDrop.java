package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import main.Assets;
import main.Camera;
import main.GameConfig;

public class WeaponDrop {
    public final Weapon weapon;
    private final int x, y;
    private static final int W = GameConfig.DROP_W, H = GameConfig.DROP_H;

    public WeaponDrop(int x, int y, Weapon weapon) {
        this.x = x;
        this.y = y;
        this.weapon = weapon;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, W, H);
    }

    public void draw(Graphics2D g2, Camera cam) {
        int cx = x + W / 2 - cam.x;
        int cy = y + H / 2 - cam.y;

        BufferedImage icon = Assets.weaponIcon(weapon);
        if (icon != null) {
            int w = icon.getWidth() * GameConfig.SCALE, h = icon.getHeight() * GameConfig.SCALE;
            g2.drawImage(icon, cx - w / 2, cy - h / 2, w, h, null);
        } else {
            g2.setColor(Color.green);
            g2.fillRect(x - cam.x, y - cam.y, W, H);
        }
        g2.setColor(Color.white);
        g2.drawString(weapon.displayName(), x - cam.x + 2, y - cam.y - 4);
    }
}