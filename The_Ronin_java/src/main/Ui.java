package main;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** เครื่องมือวาดตัวหนังสือที่หน้าเมนูใช้ร่วมกัน */
final class Ui {
    private Ui() { }

    static final Color GOLD = new Color(230, 200, 120);
    static final Color GOLD_DARK = new Color(150, 120, 50);
    static final Color PANEL = new Color(20, 20, 28, 225);
    static final Color PANEL_HOVER = new Color(62, 50, 24, 235);

    /** เปิด anti-alias ให้ตัวหนังสืออ่านง่าย (มีผลเฉพาะ Graphics ของเฟรมนี้) */
    static void smoothText(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    /** วาดข้อความให้กึ่งกลางอยู่ที่ cx (baseline = เส้นฐานของตัวอักษร) */
    static void centered(Graphics2D g2, String text, int cx, int baseline, Color color) {
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(color);
        g2.drawString(text, cx - fm.stringWidth(text) / 2, baseline);
    }

    /** ข้อความกึ่งกลางพร้อมเงาด้านหลัง ไว้ทำหัวข้อใหญ่ */
    static void centeredShadow(Graphics2D g2, String text, int cx, int baseline, Color color) {
        centered(g2, text, cx + 4, baseline + 4, new Color(0, 0, 0, 190));
        centered(g2, text, cx, baseline, color);
    }

    /** ข้อความชิดซ้ายพร้อมเงาบางๆ (อ่านได้บนงานศิลป์ทุกสี) */
    static void shadowText(Graphics2D g2, String text, int x, int baseline, Color color) {
        g2.setColor(new Color(0, 0, 0, 200));
        g2.drawString(text, x + 1, baseline + 1);
        g2.setColor(color);
        g2.drawString(text, x, baseline);
    }

    /** กึ่งกลางพร้อมเงาบางๆ */
    static void centeredThin(Graphics2D g2, String text, int cx, int baseline, Color color) {
        int x = cx - g2.getFontMetrics().stringWidth(text) / 2;
        shadowText(g2, text, x, baseline, color);
    }

    /** ชิดขวาพร้อมเงาบางๆ */
    static void rightThin(Graphics2D g2, String text, int rightX, int baseline, Color color) {
        shadowText(g2, text, rightX - g2.getFontMetrics().stringWidth(text), baseline, color);
    }

    /** ชิดขวา: ขอบขวาของข้อความอยู่ที่ rightX */
    static void rightAligned(Graphics2D g2, String text, int rightX, int baseline, Color color) {
        FontMetrics fm = g2.getFontMetrics();
        g2.setColor(color);
        g2.drawString(text, rightX - fm.stringWidth(text), baseline);
    }
}