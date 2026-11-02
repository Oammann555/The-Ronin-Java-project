package main;

import entity.Player;

public class Camera {

    public int x;
    public int y;

    GamePanel gp;

    public Camera(GamePanel gp) {
        this.gp = gp;
    }

    public void update(Player player) {

        x = (int) player.getCenterX() - gp.screenWidth / 2;
        y = 0;
        if (x < 0) {
            x = 0;
        }
        if (x > gp.stageWidth - gp.screenWidth) {
            x = gp.stageWidth - gp.screenWidth;
        }
    }
}