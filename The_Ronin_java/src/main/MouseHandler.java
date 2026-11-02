package main;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseHandler extends MouseAdapter {

    public volatile boolean leftClicked;  // one-shot: ผู้ใช้ต้องรีเซ็ตเอง
    public volatile boolean blocking;     // กดค้างเพื่อป้องกัน

    /**
     * เวลา (ms) ของการคลิกขวาครั้งล่าสุดที่นับเป็น "การพยายามปัดป้อง" — บันทึกตอนเกิด event จริง
     * (ไม่ใช่ตอน game loop อ่านค่า) จังหวะจึงแม่นยำระดับ ms และการกดค้างไม่ทำให้ค่านี้ใหม่ขึ้น
     * คลิกถี่เกินไป (ภายใน PARRY_COOLDOWN_MS) จะไม่นับเป็นความพยายามใหม่
     */
    public volatile long blockPressedAt = 0;

    // ตำแหน่งเมาส์บนจอ (ใช้เล็งธนู)
    public volatile int mouseX, mouseY;

    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (e.getButton() == MouseEvent.BUTTON1) leftClicked = true;
        if (e.getButton() == MouseEvent.BUTTON3) {
            blocking = true;
            long now = System.currentTimeMillis();
            if (now - blockPressedAt >= GameConfig.PARRY_COOLDOWN_MS) blockPressedAt = now;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) blocking = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {   // ลากขณะกดปุ่ม ก็ยังต้องอัปเดตตำแหน่ง
        mouseX = e.getX();
        mouseY = e.getY();
    }
}
