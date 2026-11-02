package main;

import java.awt.Graphics2D;

/**
 * หน้าจอที่ไม่ใช่ตอนเล่น (เมนูหลัก, เลือกอาวุธ, ...)
 *
 * GamePanel ถือ Screen ไว้ทีละหน้า: ถ้ามีอยู่ = กำลังอยู่ในเมนู, ถ้าเป็น null = กำลังเล่นเกม
 * แต่ละหน้ารู้วิธีอัปเดตและวาดตัวเอง ส่วนการสลับหน้าทำผ่านเมธอดของ GamePanel
 * (showMainMenu / showWeaponSelect / startGame) — เป็นรูปแบบ State pattern
 * เพิ่มหน้าใหม่ (เช่น เลือกด่าน) = สร้างคลาสที่ implements Screen เพิ่มหนึ่งคลาส
 */
interface Screen {
    void update();
    void draw(Graphics2D g2);
}
