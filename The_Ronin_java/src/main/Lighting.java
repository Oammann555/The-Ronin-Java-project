package main;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entity.Player;

/**
 * ความมืดพร้อม "bubble แสง" กลมๆ ที่ค่อยๆ จางที่ขอบ — ใช้กับแมพที่มืด (ตอนนี้คือ Map2)
 *
 * วิธีทำ: ปูชั้นสีเข้มโปร่งแสงคลุมทั้งจอ แล้ว "เจาะรู" เป็นวงกลมไล่ระดับตรงตำแหน่งแสง
 * ด้วย AlphaComposite.DstOut (ลบความทึบออกตามความเข้มของ gradient) ชั้นนี้ถูกวาดทับโลกเกม
 * แต่อยู่ใต้ HUD จึงไม่ทำให้ตัวหนังสือมืดตาม
 *
 * เรื่องความเร็ว: ความมืดเป็นภาพนุ่มๆ ไม่ต้องคมระดับพิกเซล จึงคำนวณที่ความละเอียด 1/DOWN
 * แล้วขยายตอนวาดลงจอ (เร็วกว่าราว 4 เท่า ที่ DOWN = 2) และวงแสงสร้างเป็นภาพสำเร็จไว้ใช้ซ้ำ
 */
public final class Lighting {

    private record Light(double x, double y, int radius) { }   // พิกัดโลก

    private static final int DOWN = 2;   // ลดความละเอียดของชั้นความมืดลงกี่เท่า (1 = เต็มความละเอียด)

    private final float darkness;
    private final List<Light> lights = new ArrayList<>();
    private BufferedImage layer;   // ชั้นความมืด (ขนาด 1/DOWN ของจอ) สร้างครั้งเดียวใช้ซ้ำทุกเฟรม

    // ภาพวงแสงที่ไล่ระดับแล้ว สร้างครั้งเดียวต่อรัศมี
    private final Map<Integer, BufferedImage> holeCache = new HashMap<>();
    private final Map<Integer, BufferedImage> glowCache = new HashMap<>();

    private Lighting(float darkness) {
        this.darkness = darkness;
    }

    /**
     * คืน Lighting ของแมพนั้น หรือ null ถ้าแมพไม่มืด
     * scale = อัตราที่พื้นหลังถูกขยายจากรูปต้นฉบับ (screenHeight / ความสูงรูป)
     */
    public static Lighting forMap(String bgPath, double scale) {
        if (bgPath.endsWith("Map2.png")) {
            Lighting l = new Lighting(GameConfig.DARKNESS);
            // โคมไฟ 3 ดวงที่วาดไว้ใน Map2.png (พิกัดในรูปต้นฉบับ 512x128 วัดจากพิกเซลสว่างของโคม)
            double[][] lanterns = { {37.5, 92.5}, {207.5, 92.5}, {377.5, 92.5} };
            for (double[] p : lanterns) {
                l.lights.add(new Light(p[0] * scale, p[1] * scale, GameConfig.LANTERN_RADIUS));
            }
            return l;
        }
        return null;
    }

    public void draw(Graphics2D g2, Camera cam, Player player) {
        final int w = GameConfig.SCREEN_WIDTH, h = GameConfig.SCREEN_HEIGHT;
        final int lw = w / DOWN, lh = h / DOWN;
        if (layer == null) layer = new BufferedImage(lw, lh, BufferedImage.TYPE_INT_ARGB_PRE);   // premultiplied = blend เร็วกว่า

        Graphics2D lg = layer.createGraphics();
        try {
            // 1) ปูความมืดทั้งชั้น (Src = เขียนทับของเฟรมก่อนทั้งหมด)
            lg.setComposite(AlphaComposite.Src);
            lg.setColor(new Color(4, 6, 20, Math.round(255 * darkness)));
            lg.fillRect(0, 0, lw, lh);

            // 2) เจาะรูแสง: โคมไฟ + รอบตัวผู้เล่น (พิกัดและรัศมีหารด้วย DOWN)
            lg.setComposite(AlphaComposite.DstOut);
            for (Light l : lights) {
                punch(lg, (l.x() - cam.x) / DOWN, (l.y() - cam.y) / DOWN, l.radius() / DOWN, lw);
            }
            if (GameConfig.PLAYER_LIGHT_RADIUS > 0) {
                punch(lg, (player.getCenterX() - cam.x) / DOWN,
                          (player.getY() + player.getHeight() / 2.0 - cam.y) / DOWN,
                          GameConfig.PLAYER_LIGHT_RADIUS / DOWN, lw);
            }

            // 3) แสงส้มอุ่นจางๆ ในวงของโคมไฟ ให้ดูเป็นแสงไม่ใช่แค่รูโปร่ง
            lg.setComposite(AlphaComposite.SrcOver);
            for (Light l : lights) {
                glow(lg, (l.x() - cam.x) / DOWN, (l.y() - cam.y) / DOWN, l.radius() / DOWN, lw);
            }
        } finally {
            lg.dispose();
        }

        // ขยายกลับเต็มจอด้วย nearest-neighbor (ตามที่ GamePanel ตั้งไว้) — ความมืดเป็นการไล่ระดับที่นุ่มมาก
        // ขั้นบันได 2 พิกเซลจึงมองไม่เห็น ส่วน bilinear ช้ากว่านี้หลายเท่าบน Java2D
        g2.drawImage(layer, 0, 0, w, h, null);
    }

    /** เจาะรูแสง: วาดภาพวงแสงด้วย DstOut (ต้องตั้ง composite เป็น DstOut ไว้ก่อนเรียก) */
    private void punch(Graphics2D g, double cx, double cy, int r, int layerW) {
        if (cx + r < 0 || cx - r > layerW) return;   // อยู่นอกจอ ข้ามได้
        BufferedImage img = holeCache.computeIfAbsent(r,
            k -> radial(k, new Color(0, 0, 0, 255), new Color(0, 0, 0, 215), new Color(0, 0, 0, 0), 0.45f));
        g.drawImage(img, (int) (cx - r), (int) (cy - r), null);
    }

    private void glow(Graphics2D g, double cx, double cy, int r, int layerW) {
        if (cx + r < 0 || cx - r > layerW) return;
        BufferedImage img = glowCache.computeIfAbsent(r,
            k -> radial(k, new Color(255, 200, 90, 70), new Color(255, 200, 90, 35), new Color(255, 200, 90, 0), 0.45f));
        g.drawImage(img, (int) (cx - r), (int) (cy - r), null);
    }

    /** วาดวงกลมไล่ระดับสี (กลาง → กลางรัศมี → ขอบ) ลงภาพขนาด 2r x 2r ครั้งเดียว */
    private static BufferedImage radial(int r, Color center, Color mid, Color edge, float midAt) {
        BufferedImage img = new BufferedImage(r * 2, r * 2, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = img.createGraphics();
        g.setPaint(new RadialGradientPaint(
            new Point2D.Double(r, r), r,
            new float[] { 0f, midAt, 1f },
            new Color[] { center, mid, edge }));
        g.fillRect(0, 0, r * 2, r * 2);
        g.dispose();
        return img;
    }
}
