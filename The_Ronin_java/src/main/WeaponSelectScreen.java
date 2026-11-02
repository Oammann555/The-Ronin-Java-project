package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.text.BreakIterator;
import java.util.Locale;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;


import entity.Weapon;


final class WeaponSelectScreen implements Screen {

    //เลือกอาวุธตอนเริ่ม
    private static final Weapon[] CHOICES = {
        Weapon.KATANA, Weapon.YARI, Weapon.NAGINATA, Weapon.YUMI, Weapon.NINJAKATANA
    };

    private static final int CARD_W = 176, CARD_H = 390, GAP = 16, CARD_Y = 180;

    private static final float FRAME_BOTTOM_MARGIN = 0.07f;
    private static final float SIDE_PAD = 0.02f;
    private static final float INNER_BOTTOM_PAD = 0.056f;

    private final GamePanel gp;
    private final float k;
    private final Rectangle[] cards = new Rectangle[CHOICES.length];
    private final Rectangle frameRect;
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

        Rectangle region;
        int cardY;
        if (frame != null) {
            int fw = Math.round(frame.getWidth() * k), fh = Math.round(frame.getHeight() * k);
            int fx = (w - fw) / 2;
            int fy = Math.max(0, h - fh - Math.round(h * FRAME_BOTTOM_MARGIN));
            frameRect = new Rectangle(fx, fy, fw, fh);
            region = frameRect;
            cardY = Math.max(fy, fy + fh - ch - Math.round(fh * INNER_BOTTOM_PAD));
        } else {
            frameRect = null;
            region = new Rectangle(0, 0, w, h);
            cardY = CARD_Y;
        }

        int pad = frame != null ? Math.round(region.width * SIDE_PAD) : 0;
        int gap = GAP;
        if (inner != null || frame != null) {
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

        boolean art = frame != null || bg != null;
        back = art ? new MenuButton("BACK", Assets.backButton, k, 24 + 70, 20, 140, 44)
                   : new MenuButton("BACK", Assets.backButton, k, w / 2, 650, 200, 52);
    }

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

        // การ์ดอาวุธ
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

        // กรอบโชว์อาวุธ
        Rectangle show;
        BufferedImage box = Assets.weaShowBox;
        if (box != null) {
            int bw = Math.round(box.getWidth() * k), bh = Math.round(box.getHeight() * k);
            int top = (r.width - bw) / 2;
            show = new Rectangle(mid - bw / 2, r.y + top, bw, bh);
            g2.drawImage(box, show.x, show.y, show.width, show.height, null);
        } else {
            show = new Rectangle(r.x + 24, r.y + 20, r.width - 48, 130);
        }

        drawWeaponPicture(g2, wp, show);

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
        
        ty += 8;
        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        g2.setColor(new Color(225, 218, 200));
        drawDescription(g2, description(wp), left, ty, right - left, r.y + r.height - 12);
    }

    //weapon description
    private static String description(Weapon w) {
        return switch (w) {
            case KATANA -> "A master-crafted sword\noffering a lethal balance of power and speed.\nIt is the true pride of the samurai!";
            case YARI -> "\ncommonly wielded by the Ashigaru Though lacking the elegance of a katana, it is very powerful and effective.";
            case NAGINATA -> "A versatile long-handled weapon widely used by warrior monks.\nWhile wielded, it continuously regenerates the user's health.";
            case YUMI -> "A ranged weapon capable of striking targets from afar. Its damage may be low but annoying";
            case NINJAKATANA -> "specialized blade for ninjas. Though less powerful than a katana, it is extremely lightweight and agile";
        };
    }

    private static void drawDescription(Graphics2D g2, String text, int x, int y,
                                        int maxWidth, int bottomY) {
        FontMetrics fm = g2.getFontMetrics();
        int lineHeight = fm.getHeight() + 2;
        int maxLines = Math.max(0, (bottomY - y) / lineHeight);
        int line = 0;
        for (String paragraph : text.split("\\n", -1)) {
            StringBuilder current = new StringBuilder();
            BreakIterator words = BreakIterator.getLineInstance(Locale.forLanguageTag("th"));
            words.setText(paragraph);
            int start = words.first();
            for (int end = words.next(); end != BreakIterator.DONE; start = end, end = words.next()) {
                String part = paragraph.substring(start, end);
                String candidate = current + part;
                if (fm.stringWidth(candidate.trim()) > maxWidth && current.length() > 0) {
                    if (line >= maxLines) return;
                    g2.drawString(current.toString().trim(), x, y + line * lineHeight);
                    line++;
                    current.setLength(0);
                    current.append(part.stripLeading());
                } else {
                    current.append(part);
                }
            }
            if (current.length() > 0) {
                if (line >= maxLines) return;
                g2.drawString(current.toString().trim(), x, y + line * lineHeight);
                line++;
            }
            if (line >= maxLines) return;
        }
    }

    /**
     * วาดรูปอาวุธให้อยู่กึ่งกลางกรอบโชว์
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
}