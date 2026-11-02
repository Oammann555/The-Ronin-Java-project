package main;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

final class MainMenuScreen implements Screen {
    private final GamePanel gp;
    private final MenuButton start;
    private final MenuButton exit;

    MainMenuScreen(GamePanel gp) {
        this.gp = gp;
        final int w = GameConfig.SCREEN_WIDTH, h = GameConfig.SCREEN_HEIGHT;

        BufferedImage bg = Assets.menuScreen;
        float k = bg != null ? w / (float) bg.getWidth() : GameConfig.UI_SCALE_DEFAULT;

        int startX = w / 2;
        int startY = Math.round(h * 0.52f);
        start = new MenuButton("START", Assets.startButton, k, startX, startY, 280, 64);

        int exitX = w / 2;
        int exitY = start.bounds.y + start.bounds.height + Math.round(GameConfig.MENU_BUTTON_GAP_PX * k);
        exit = new MenuButton("EXIT", Assets.exitButton, k, exitX, exitY, 280, 64);
    }

    @Override
    public void update() {
        MouseHandler m = gp.mouseH;
        start.update(m.mouseX, m.mouseY);
        exit.update(m.mouseX, m.mouseY);

        boolean clicked = m.leftClicked;
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