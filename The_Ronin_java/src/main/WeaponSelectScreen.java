package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import entity.Weapon;

/**
 * หน้าเลือกอาวุธเริ่มต้น: การ์ดอาวุธ 5 ใบ คลิกใบไหนก็เริ่มเกมด้วยอาวุธนั้น
 * ตัวเลขและคุณสมบัติบนการ์ดอ่านจาก enum Weapon ตรงๆ จึงตรงกับของจริงในเกมเสมอ
 *
 * ชั้นของภาพ (ไฟล์ไหนไม่มี → วาดแบบสำรองด้วยโค้ดเฉพาะส่วนนั้น):
 *   weaBackground  พื้นหลังเต็มจอ — ความกว้างของรูปนี้กำหนดสเกลของทั้งหน้า
 *   weaFrame       กรอบใหญ่ที่รวมการ์ดทั้งหมด
 *   weaInnerFrame  กรอบการ์ดของอาวุธแต่ละชิ้น (วาด 5 ครั้ง)
 *   weaShowBox     กรอบสำหรับโชว์รูปอาวุธ อยู่บนสุดของการ์ด
 *   wea<ชื่อ>      รูปอาวุธ: weaKatana, weaYari, weaNaginata, weaYumi, weaNindachi
 */
final class WeaponSelectScreen implements Screen {

    /** อาวุธที่เลือกได้ตอนเริ่ม และลำดับการแสดง (ไม่ใช้ Weapon.values() เพื่อคุมลำดับ/ตัดอาวุธที่ไม่อยากให้เลือก) */
    private static final Weapon[] CHOICES = {
        Weapon.KATANA, Weapon.YARI, Weapon.NAGINATA, Weapon.YUMI, Weapon.NINJAKATANA
    };

    // ---------- ขนาดแบบสำรอง (ตอนไม่มีรูปกรอบ) ----------
    private static final int CARD_W = 176, CARD_H = 390, GAP = 16, CARD_Y = 180;

    // ---------- สัดส่วนตอนจัดตำแหน่งอัตโนมัติ (วัดจากแบบร่างของคุณ) ----------
    private static final float FRAME_BOTTOM_MARGIN = 0.07f;  // weaFrame เว้นจากขอบล่างจอ (สัดส่วนความสูงจอ)
    private static final float SIDE_PAD = 0.02f;             // การ์ดเว้นจากขอบซ้าย-ขวาของกรอบ (สัดส่วนความกว้างกรอบ)
    private static final float INNER_BOTTOM_PAD = 0.056f;    // การ์ดเว้นจากขอบล่างของกรอบ (สัดส่วนความสูงกรอบ)

    private final GamePanel gp;
    private final float k;                    // สเกลของทั้งหน้า (ความกว้างจอ / ความกว้าง weaBackground)
    private final Rectangle[] cards = new Rectangle[CHOICES.length];
    private final Rectangle frameRect;        // null ถ้าไม่มี weaFrame
    private final MenuButton back;
    private int hovered = -1;

    WeaponSelectScreen(GamePanel gp) {
        this.gp = gp;
        final int w = GameConfig.SCREEN_WIDTH, h = GameConfig.SCREEN_HEIGHT;
        final int n = CHOICES.length;

        BufferedImage bg = Assets.weaBackground, frame = Assets.weaFrame, inner = Assets.weaInnerFrame;
        k = bg != null ? w / (float) bg.getWidth() : GameConfig.UI_SCALE_DEFAULT;

        int cw = inner != null ? Math.round(inner.getWidth() * k) : CARD_W;
        int ch = inner != null ? Math.round(inner.getHeight() * k) : CARD_H;

        // พื้นที่ที่การ์ดทั้ง 5 วางอยู่: ในกรอบใหญ่ (ถ้ามี) หรือทั้งจอ
        Rectangle region;
        int cardY;
        if (frame != null) {
            int fw = Math.round(frame.getWidth() * k), fh = Math.round(frame.getHeight() * k);
            int fx = GameConfig.WEA_FRAME_X_PX < 0 ? (w - fw) / 2 : Math.round(GameConfig.WEA_FRAME_X_PX * k);
            int fy = GameConfig.WEA_FRAME_Y_PX < 0
                    ? Math.max(0, h - fh - Math.round(h * FRAME_BOTTOM_MARGIN))   // ชิดล่าง เหลือที่ด้านบนไว้ให้ชื่อหน้าในรูปพื้นหลัง
                    : Math.round(GameConfig.WEA_FRAME_Y_PX * k);
            frameRect = new Rectangle(fx, fy, fw, fh);
            region = frameRect;
            cardY = GameConfig.WEA_INNER_TOP_PX < 0
                    ? Math.max(fy, fy + fh - ch - Math.round(fh * INNER_BOTTOM_PAD))
                    : fy + Math.round(GameConfig.WEA_INNER_TOP_PX * k);
        } else {
            frameRect = null;
            region = new Rectangle(0, 0, w, h);
            cardY = CARD_Y;
        }

        // กระจายการ์ดให้ช่องว่างเท่ากันทั้งแถว (หรือใช้ระยะที่กำหนดเองใน GameConfig)
        int pad = frame != null ? Math.round(region.width * SIDE_PAD) : 0;
        int gap = GAP;
        if (GameConfig.WEA_INNER_GAP_PX >= 0) {
            gap = Math.round(GameConfig.WEA_INNER_GAP_PX * k);
        } else if (inner != null || frame != null) {
            int free = region.width - 2 * pad - n * cw;
            if (free < 0) {
                System.err.println("[UI] weaInnerFrame.png กว้างเกินกว่าจะวาง 5 ใบในกรอบ — ลดความกว้างรูป หรือขยาย weaFrame.png");
            }
            gap = Math.max(0, free / (n - 1));
        }
        int total = n * cw + (n - 1) * gap;
        int left = region.x + (region.width - total) / 2;
        for (int i = 0; i < n; i++) {
            cards[i] = new Rectangle(left + i * (cw + gap), cardY, cw, ch);
        }

        // ปุ่ม BACK: ถ้าหน้านี้มีรูปที่วาดเอง วางมุมซ้ายบนไม่ให้ชนกรอบ ไม่งั้นอยู่ล่างกลางจอ (backbutton.png ไม่บังคับ)
        boolean art = frame != null || bg != null;
        back = art ? new MenuButton("BACK", Assets.backButton, k, 24 + 70, 20, 140, 44)
                   : new MenuButton("BACK", Assets.backButton, k, w / 2, 650, 200, 52);
    }

    /** ไว้ให้เทสต์ตรวจตำแหน่งการ์ด */
    Rectangle cardBounds(int i) { return cards[i]; }

    @Override
    public void update() {
        MouseHandler m = gp.mouseH;
        hovered = -1;
        for (int i = 0; i < cards.length; i++) {
            if (cards[i].contains(m.mouseX, m.mouseY)) hovered = i;
        }
        back.update(m.mouseX, m.mouseY);

        boolean clicked = m.leftClicked;
        m.leftClicked = false;
        if (!clicked) return;

        if (hovered >= 0) gp.startGame(CHOICES[hovered]);
        else if (back.hovered) gp.showMainMenu();
    }

    @Override
    public void draw(Graphics2D g2) {
        final int w = GameConfig.SCREEN_WIDTH, h = GameConfig.SCREEN_HEIGHT;

        boolean hasBg = Assets.weaBackground != null;
        if (hasBg) g2.drawImage(Assets.weaBackground, 0, 0, w, h, null);
        else gp.drawBackdrop(g2);
        Ui.smoothText(g2);

        if (!hasBg || GameConfig.UI_TITLE_TEXT_OVER_ART) {
            g2.setFont(new Font("Arial", Font.BOLD, 44));
            Ui.centeredShadow(g2, "CHOOSE YOUR WEAPON", w / 2, 120, Ui.GOLD);
            g2.setFont(new Font("Arial", Font.PLAIN, 18));
            Ui.centered(g2, "Click a weapon to begin", w / 2, 152, new Color(200, 200, 205));
        }

        if (frameRect != null) {
            g2.drawImage(Assets.weaFrame, frameRect.x, frameRect.y, frameRect.width, frameRect.height, null);
        }
        for (int i = 0; i < cards.length; i++) {
            drawCard(g2, cards[i], CHOICES[i], i == hovered);
        }
        back.draw(g2);
    }

    private void drawCard(Graphics2D g2, Rectangle r, Weapon wp, boolean hot) {
        int mid = r.x + r.width / 2;

        // 1) กรอบการ์ด (hover = สำเนาที่สว่างขึ้น)
        BufferedImage inner = Assets.weaInnerFrame;
        if (inner != null) {
            g2.drawImage(hot ? Assets.brightened(inner) : inner, r.x, r.y, r.width, r.height, null);
        } else {
            g2.setColor(hot ? Ui.PANEL_HOVER : Ui.PANEL);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 18, 18);
            g2.setColor(hot ? Ui.GOLD : Ui.GOLD_DARK);
            g2.setStroke(new BasicStroke(hot ? 4 : 2));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 18, 18);
        }

        // 2) กรอบโชว์อาวุธ (อยู่บนสุดของการ์ด ตรงกลาง)
        Rectangle show;
        BufferedImage box = Assets.weaShowBox;
        if (box != null) {
            int bw = Math.round(box.getWidth() * k), bh = Math.round(box.getHeight() * k);
            int top = GameConfig.WEA_SHOWBOX_TOP_PX < 0 ? (r.width - bw) / 2 : Math.round(GameConfig.WEA_SHOWBOX_TOP_PX * k);
            show = new Rectangle(mid - bw / 2, r.y + top, bw, bh);
            g2.drawImage(box, show.x, show.y, show.width, show.height, null);
        } else {
            show = new Rectangle(r.x + 24, r.y + 20, r.width - 48, 130);
        }

        // 3) รูปอาวุธ
        drawWeaponPicture(g2, wp, show);

        // 4) ชื่อ + ตัวเลข + คุณสมบัติ อยู่ใต้กรอบโชว์ (เงาบางๆ ให้อ่านได้บนงานศิลป์ทุกสี)
        int y0 = show.y + show.height;
        g2.setFont(new Font("Arial", Font.BOLD, 26));
        Ui.centeredThin(g2, wp.displayName(), mid, y0 + 28, hot ? Color.white : Ui.GOLD);

        int left = r.x + 16, right = r.x + r.width - 16, y = y0 + 70;
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        Color label = new Color(185, 185, 192);
        stat(g2, "Damage", String.valueOf(wp.damage), left, right, y, label);
        stat(g2, "Range", String.valueOf(wp.range), left, right, y + 26, label);
        stat(g2, "Cooldown", String.format("%.2fs", wp.cooldownMs / 1000.0), left, right, y + 52, label);

        // คุณสมบัติพิเศษ — สร้างจากค่าใน Weapon อัตโนมัติ
        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        int ty = y + 92;
        for (String t : traits(wp)) {
            Ui.centeredThin(g2, t, mid, ty, new Color(150, 220, 160));
            ty += 22;
        }
    }

    /**
     * วาดรูปอาวุธให้อยู่กึ่งกลางกรอบโชว์
     *  - รูปที่วาดเอง (weaXxx.png): ขยายด้วยสเกลของหน้า ถ้าใหญ่เกินกรอบค่อยย่อให้พอดี
     *  - ไม่มี: ใช้รูปอาวุธในเกม (ตัดขอบโปร่งใส) ขยายเป็นจำนวนเต็มเท่าที่ใหญ่ที่สุดที่พอดีกรอบ
     */
    private void drawWeaponPicture(Graphics2D g2, Weapon wp, Rectangle show) {
        int margin = Math.round(k * 2);
        int availW = show.width - margin * 2, availH = show.height - margin * 2;

        BufferedImage art = Assets.weaImage(wp);
        BufferedImage img;
        int dw, dh;
        if (art != null) {
            img = art;
            double fit = Math.min(1.0, Math.min(availW / (double) Math.round(art.getWidth() * k),
                                                availH / (double) Math.round(art.getHeight() * k)));
            dw = (int) Math.round(art.getWidth() * k * fit);
            dh = (int) Math.round(art.getHeight() * k * fit);
        } else {
            img = Assets.weaponIcon(wp);
            if (img == null) return;
            int sc = Math.max(1, Math.min(availW / img.getWidth(), Math.min(availH, 100) / img.getHeight()));
            dw = img.getWidth() * sc;
            dh = img.getHeight() * sc;
        }
        g2.drawImage(img, show.x + (show.width - dw) / 2, show.y + (show.height - dh) / 2, dw, dh, null);
    }

    private static void stat(Graphics2D g2, String name, String value, int left, int right, int y, Color label) {
        Ui.shadowText(g2, name, left, y, label);
        Ui.rightThin(g2, value, right, y, Color.white);
    }

    private static List<String> traits(Weapon w) {
        List<String> t = new ArrayList<>();
        if (w.ranged) t.add("Aim with mouse");
        if (w.lunges()) t.add("Thrust attack");
        if (w.deflectsArrows) t.add("Deflects arrows");
        if (w.healAmount > 0) t.add("+" + w.healAmount + " HP every 3s");
        return t;
    }
}