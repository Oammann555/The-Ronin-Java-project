package entity;

import main.Assets;
import main.GamePanel;

public class Samurai extends Enemy {

    public Samurai(GamePanel gp, Player player, int x, Weapon weapon) {
        super(gp, player, x, weapon);

        sprite = Assets.samurai;

        speed = 3;
        sightRange = 450;   // เห็นผู้เล่นไกลสุดกี่พิกเซล (ด้านหน้า)
        maxHP = 200;
        HP = maxHP;

        y = gp.groundY - height;
    }
}