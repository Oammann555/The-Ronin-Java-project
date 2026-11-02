package entity;

import main.Assets;
import main.GamePanel;

public class Ashigaru extends Enemy {

    public Ashigaru(GamePanel gp, Player player, int x, Weapon weapon) {
        super(gp, player, x, weapon);

        sprite = Assets.ashigaru;

        speed = 2;
        sightRange = 450;   // เห็นผู้เล่นไกลสุดกี่พิกเซล (ด้านหน้า)
        maxHP = 50;
        HP = maxHP;

        y = gp.groundY - height;
    }
}