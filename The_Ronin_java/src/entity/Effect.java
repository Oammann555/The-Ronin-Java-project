package entity;

import java.awt.Graphics2D;

import main.Camera;

/**
 * เอฟเฟกต์ลอยๆ ในโลกเกมที่มีอายุจำกัด (ควันวาร์ป, ประกายตอนปัดป้อง ...)
 * GamePanel เก็บไว้ในลิสต์เดียว วาดทับโลกเกม และลบทิ้งเมื่อ isFinished() เป็นจริง
 * เพิ่มเอฟเฟกต์ใหม่ = สร้างคลาสที่ implements Effect ไม่ต้องแก้ GamePanel
 */
public interface Effect {
    boolean isFinished();
    void draw(Graphics2D g2, Camera cam);
}
