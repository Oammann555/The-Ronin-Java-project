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

public final class Lighting {

    private record Light(double x, double y, int radius) { }

    private static final int DOWN = 2;

    private final float darkness;
    private final List<Light> lights = new ArrayList<>();
    private BufferedImage layer;
    private final Map<Integer, BufferedImage> holeCache = new HashMap<>();
    private final Map<Integer, BufferedImage> glowCache = new HashMap<>();

    private Lighting(float darkness) {
        this.darkness = darkness;
    }

    public static Lighting forMap(String bgPath, double scale) {
        if (bgPath.endsWith("Map2.png")) {
            Lighting l = new Lighting(GameConfig.DARKNESS);
            // ไฟของแต่ละโคมไฟในแมพนินจา
            double[][] lanterns = { {91.5, 92.5}, {261.5, 92.5}, {431.5, 92.5} };
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
        if (layer == null) layer = new BufferedImage(lw, lh, BufferedImage.TYPE_INT_ARGB_PRE);

        Graphics2D lg = layer.createGraphics();
        try {
            lg.setComposite(AlphaComposite.Src);
            lg.setColor(new Color(4, 6, 20, Math.round(255 * darkness)));
            lg.fillRect(0, 0, lw, lh);

            // ออร่าแสงรอบplayer
            lg.setComposite(AlphaComposite.DstOut);
            for (Light l : lights) {
                punch(lg, (l.x() - cam.x) / DOWN, (l.y() - cam.y) / DOWN, l.radius() / DOWN, lw);
            }
            if (GameConfig.PLAYER_LIGHT_RADIUS > 0) {
                punch(lg, (player.getCenterX() - cam.x) / DOWN,
                          (player.getY() + player.getHeight() / 2.0 - cam.y) / DOWN,
                          GameConfig.PLAYER_LIGHT_RADIUS / DOWN, lw);
            }

            //แสงส้มอุ่นจางๆ ในวงของโคมไฟ
            lg.setComposite(AlphaComposite.SrcOver);
            for (Light l : lights) {
                glow(lg, (l.x() - cam.x) / DOWN, (l.y() - cam.y) / DOWN, l.radius() / DOWN, lw);
            }
        } finally {
            lg.dispose();
        }
        g2.drawImage(layer, 0, 0, w, h, null);
    }
    private void punch(Graphics2D g, double cx, double cy, int r, int layerW) {
        if (cx + r < 0 || cx - r > layerW) return;
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
