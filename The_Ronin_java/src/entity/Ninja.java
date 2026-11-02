package entity;

import main.Assets;
import main.GamePanel;

public class Ninja extends Enemy {

    // =====================================================
    // Second Life: ตายครั้งแรกไม่ตายจริง วาร์ปหนีพร้อมเลือดกลับมา
    // =====================================================
    private boolean secondLifeUsed = false;
    private static final int WARP_MIN = 50;                  // วาร์ปไปห่างผู้เล่นอย่างน้อยกี่พิกเซล
    private static final int WARP_MAX = 200;                 // ... และไม่เกินกี่พิกเซล
    private static final double REVIVE_HP_FRACTION = 1.0;    // ฟื้นเลือดกลับมากี่ส่วนของเลือดเต็ม (1.0 = เต็ม)

    public Ninja(GamePanel gp, Player player, int x, Weapon weapon) {
        super(gp, player, x, weapon);

        sprite = Assets.ninja;

        speed = 5;
        sightRange = 750;   // เห็นผู้เล่นไกลสุดกี่พิกเซล (ด้านหน้า)
        maxHP = 70;
        HP = maxHP;

        y = gp.groundY - height;
    }

    /** ถ้าดาเมจนี้ทำให้ตายและยังไม่เคยใช้ second life → ใช้ second life แทนการตาย */
    @Override
    public void takeDamage(int damage) {
        if (!secondLifeUsed && HP - damage <= 0) {
            secondLifeUsed = true;
            useSecondLife();
            return;
        }
        super.takeDamage(damage);
    }

    private void useSecondLife() {
        // 1) ควันตรงจุดที่หายตัว
        gp.effects.add(new SmokePuff(getCenterX(), y + height / 2.0));

        // 2) วาร์ปไปห่างผู้เล่น WARP_MIN–WARP_MAX พิกเซล ซ้ายหรือขวาแบบสุ่ม
        double dist = WARP_MIN + Math.random() * (WARP_MAX - WARP_MIN);
        int side = Math.random() < 0.5 ? -1 : 1;
        double minC = width / 2.0;                       // ขอบเขตของ "กึ่งกลางตัว" ในแมพ
        double maxC = gp.stageWidth - width / 2.0;
        double target = player.getCenterX() + side * dist;
        if (target < minC || target > maxC) {
            target = player.getCenterX() - side * dist;  // ฝั่งนั้นชนขอบแมพ ใช้อีกฝั่งแทน
        }
        target = Math.max(minC, Math.min(maxC, target));
        x = target - width / 2.0;
        velocityY = 0;

        // 3) เลือดกลับมา
        HP = Math.max(1, (int) Math.round(maxHP * REVIVE_HP_FRACTION));

        // 4) ควันตรงจุดที่ปรากฏตัว
        gp.effects.add(new SmokePuff(getCenterX(), y + height / 2.0));

        cancelWindup();   // กำลังง้างอยู่ก็ยกเลิก (วาร์ปหนีกลางจังหวะ)

        // 5) รู้ตัวทันที แต่ต้องรอเวลาตอบสนองสั้นๆ ก่อนตีครั้งแรก
        alert();
        long earliest = System.currentTimeMillis() + REACTION_MS - weapon.cooldownMs * 2L;
        if (lastAttack < earliest) lastAttack = earliest;
    }
}