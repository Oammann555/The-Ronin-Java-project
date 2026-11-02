package entity;

import java.awt.Rectangle;

public class Entity {
    // ใช้ double เพื่อให้ฟิสิกส์ไม่เพี้ยนจากการตัดทศนิยม
    protected double x;
    protected double y;

    protected int HP;
    protected int maxHP;

    protected int speed;

    protected int width;
    protected int height;

    protected double velocityX;
    protected double velocityY;

    protected boolean alive = true;
    protected int facing = 1;   // 1 = ขวา, -1 = ซ้าย
    protected long swingStart = 0;   // เวลา (ms) ที่เริ่มโจมตีล่าสุด ใช้แสดง effect

    public void takeDamage(int damage) {
        HP -= damage;
        if (HP <= 0) {
            HP = 0;
            alive = false;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getHP() { return HP; }
    public int getMaxHP() { return maxHP; }
    public boolean isAlive() { return alive; }
    public double getCenterX() { return x + width / 2.0; }
}