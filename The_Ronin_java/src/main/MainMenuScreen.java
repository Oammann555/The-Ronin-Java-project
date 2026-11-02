package main;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * หน้าเมนูหลัก: ปุ่ม START และ EXIT
 * ใช้ภาพที่วาดเอง (Menuscreen.png, startbutton.png, exitbutton.png) ถ้ามี ไม่งั้นวาดแบบสำรองด้วยโค้ด
 *
 * สเกล = ความกว้างจอ / ความกว้าง Menuscreen.png และทุกอย่างในหน้านี้ขยายด้วยสเกลเดียวกัน
 * ตำแหน่งปุ่มอ่านจาก GameConfig (พิกัดในรูป Menuscreen.png) ถ้าเป็น -1 จะจัดให้อัตโนมัติ
 */
final class MainMenuScreen implements Screen {
    private final GamePanel gp;
    private final MenuButton start;
    private final MenuButton exit;

    MainMenuScreen(GamePanel gp) {
        this.gp = gp;
        final int w = GameConfig.SCREEN_WIDTH, h = GameConfig.SCREEN_HEIGHT;

        BufferedImage bg = Assets.menuScreen;
        float k = bg != null ? w / (float) bg.getWidth() : GameConfig.UI_SCALE_DEFAULT;

        int startX = GameConfig.MENU_START_X_PX < 0 ? w / 2 : Math.round(GameConfig.MENU_START_X_PX * k);
        int startY = GameConfig.MENU_START_Y_PX < 0 ? Math.round(h * 0.52f) : Math.round(GameConfig.MENU_START_Y_PX * k);
        start = new MenuButton("START", Assets.startButton, k, startX, startY, 280, 64);

        int exitX = GameConfig.MENU_EXIT_X_PX < 0 ? w / 2 : Math.round(GameConfig.MENU_EXIT_X_PX * k);
        int exitY = GameConfig.MENU_EXIT_Y_PX < 0
                ? start.bounds.y + start.bounds.height + Math.round(GameConfig.MENU_BUTTON_GAP_PX * k)
                : Math.round(GameConfig.MENU_EXIT_Y_PX * k);
        exit = new MenuButton("EXIT", Assets.exitButton, k, exitX, exitY, 280, 64);
    }

    @Override
    public void update() {
        MouseHandler m = gp.mouseH;
        start.update(m.mouseX, m.mouseY);
        exit.update(m.mouseX, m.mouseY);

        boolean clicked = m.leftClicked;   // คลิกเป็นแบบ one-shot: อ่านแล้วต้องรีเซ็ตเอง
        m.leftClicked = false;
        if (!clicked) return;

        if (start.hovered) gp.showWeaponSelect();
        else if (exit.hovered) gp.quit();
    }

    @Override
    public void draw(Graphics2D g2) {
        boolean hasArt = Assets.menuScreen != null;
        if (hasArt) {
            g2.drawImage(Assets.menuScreen, 0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT, null);
        } else {
            gp.drawBackdrop(g2);
        }

        if (!hasArt || GameConfig.UI_TITLE_TEXT_OVER_ART) {
            Ui.smoothText(g2);
            g2.setFont(new Font("Arial", Font.BOLD, 104));
            Ui.centeredShadow(g2, "THE RONIN", GameConfig.SCREEN_WIDTH / 2, 270, Ui.GOLD);
        }

        start.draw(g2);
        exit.draw(g2);
    }
}