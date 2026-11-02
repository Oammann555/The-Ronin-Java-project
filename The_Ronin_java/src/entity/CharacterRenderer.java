package entity;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.Assets;
import main.GameConfig;

/**
 * วาดตัวละคร + อาวุธที่ถือ + effect ตอนโจมตี
 * ใช้ร่วมกันทั้ง Player และ Enemy จึงไม่ต้องเขียนซ้ำ
 *
 * หลักการ: รูปตัวละครและรูปอาวุธเป็นผืน 32x32 ขนาดเท่ากัน ออกแบบให้ซ้อนทับกันได้พอดี
 * จึงวาดสองรูปด้วยตำแหน่งและการกลับด้านเดียวกัน ไม่ต้องคำนวณตำแหน่งมือ
 * (ขนาดทั้งหมดอ่านจาก GameConfig)
 */
final class CharacterRenderer {
    private CharacterRenderer() { }

    static final long EFFECT_MS = 160;

    static void draw(Graphics2D g2, Entity e, int camX, int camY,
                     BufferedImage body, Color fallback, Weapon weapon, long swingStart,
                     BufferedImage armour, boolean buffed) {

        final int sprite = GameConfig.SPRITE;
        final int bodyCenter = GameConfig.BODY_CENTER;

        boolean right = e.facing == 1;
        int top = (int) e.y - camY;
        int hitLeft = (int) e.x - camX;
        // จัดให้กึ่งกลางลำตัวในรูปตรงกับกึ่งกลาง hitbox (ตอนกลับด้าน กึ่งกลางจะสลับข้าง)
        int cx = (int) e.getCenterX() - camX;
        int left = cx - (right ? bodyCenter : sprite - bodyCenter);

        long elapsed = System.currentTimeMillis() - swingStart;
        boolean swinging = elapsed >= 0 && elapsed < EFFECT_MS;
        BufferedImage fx = Assets.effect(weapon, buffed);   // buffed = ทับสีแดงตอนมียาพลังโจมตี

        // 1) ตัวละคร
        if (body != null) {
            Assets.draw(g2, body, left, top, sprite, sprite, !right);
        } else {
            g2.setColor(fallback);
            g2.fillRect(hitLeft, top, e.width, e.height);
        }

        // 1.5) ชุดเกราะ (ถ้ามี) สวมทับตัว ใต้อาวุธ — ใช้ตำแหน่งและการกลับด้านเดียวกับตัวละคร
        Assets.draw(g2, armour, left, top, sprite, sprite, !right);

        // 2) อาวุธ — ทวนและนากินาตะพุ่งไปข้างหน้านิดหน่อยตอนแทง
        int lunge = (swinging && fx != null && weapon.lunges()) ? GameConfig.LUNGE : 0;
        Assets.draw(g2, Assets.heldSprite(weapon),
                    left + (right ? lunge : -lunge), top, sprite, sprite, !right);

        // 3) effect (จางหายไปตามเวลา)
        if (swinging && fx != null) {
            int w, h;
            if (!weapon.lunges()) {                     // slash (katana, ninjakatana): ขยายเท่ากันทุกด้าน
                w = fx.getWidth() * GameConfig.SLASH_SCALE;
                h = fx.getHeight() * GameConfig.SLASH_SCALE;
            } else {                                    // stab: ยืดความกว้างตามระยะอาวุธ
                w = weapon.range;
                h = fx.getHeight() * GameConfig.SCALE;
            }
            int fy = top + GameConfig.HAND_Y - h / 2;
            int fxLeft = right ? hitLeft + e.width + 4 : hitLeft - 4 - w;

            Composite old = g2.getComposite();
            float alpha = 1f - (float) elapsed / EFFECT_MS;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            Assets.draw(g2, fx, fxLeft, fy, w, h, !right);
            g2.setComposite(old);
        }
    }
}