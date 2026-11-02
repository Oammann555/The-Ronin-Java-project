package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

import entity.Ashigaru;
import entity.Effect;
import entity.Enemy;
import entity.ItemDrop;
import entity.ItemType;
import entity.Ninja;
import entity.Player;
import entity.Projectile;
import entity.Samurai;
import entity.SmokePuff;
import entity.Effect;
import entity.ParrySpark;
import entity.WarriorMonk;
import entity.Weapon;
import entity.WeaponDrop;


public class GamePanel extends JPanel implements Runnable {
    public Camera camera;

    // ขนาดหน้าจอมาจาก GameConfig ทั้งหมด
    public final int tileSize = GameConfig.TILE_SIZE;
    public final int screenWidth = GameConfig.SCREEN_WIDTH;
    public final int screenHeight = GameConfig.SCREEN_HEIGHT;

    public int stageWidth = screenWidth;   // fallback ถ้าโหลดภาพไม่ได้

    BufferedImage background;

    KeyHandler keyH = new KeyHandler();
    MouseHandler mouseH = new MouseHandler();
    Thread gameThread;

    Player player;

    // ใช้ CopyOnWriteArrayList เพราะ update รันบน game thread แต่ paint รันบน EDT
    public List<Enemy> enemies = new CopyOnWriteArrayList<>();
    public List<Projectile> projectiles = new CopyOnWriteArrayList<>();
    public List<WeaponDrop> drops = new CopyOnWriteArrayList<>();
    public List<ItemDrop> items = new CopyOnWriteArrayList<>();
   
    public List<Effect> effects = new CopyOnWriteArrayList<>();
    Lighting lighting;   // null = ด่านนี้ไม่มืด

    // ---------- เมนู ----------
    Screen screen;                          // หน้าเมนูที่แสดงอยู่ (null = กำลังเล่นเกม)
    Weapon startWeapon = Weapon.KATANA;     // อาวุธที่ผู้เล่นเลือกตอนเริ่มเกม (ใช้ตอนกด R เริ่มใหม่ด้วย)

    List<Level> levels = Level.createAll();
    int levelIndex = 0;
    boolean allCleared = false;

    public double gravity = 0.53;
    int FPS = 60;
    public int groundY = GameConfig.GROUND_Y;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);

        Assets.load();   // ต้องโหลดรูปก่อนสร้างตัวละคร

        camera = new Camera(this);
        player = new Player(this, keyH, mouseH);

        this.addKeyListener(keyH);
        this.addMouseListener(mouseH);
        this.addMouseMotionListener(mouseH);   // ไว้เล็งธนู
        this.setFocusable(true);

        // เริ่มที่เมนูหลัก (โหลดแค่ภาพพื้นหลังด่าน 1 มาเป็นฉากหลังเมนู ตัวด่านจริงโหลดตอนกดเริ่มเกม)
        loadStage(levels.get(0).bgPath);
        screen = new MainMenuScreen(this);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    // ---------------------------------------------------------
    // Level
    // ---------------------------------------------------------
    public void loadLevel(int index) {
        levelIndex = index;
        Level lv = levels.get(index);

        enemies.clear();
        projectiles.clear();
        drops.clear();
        items.clear();
        effects.clear();

        loadStage(lv.bgPath);

        for (EnemySpawn s : lv.spawns) {
            int ex = Math.min(s.x(), stageWidth - tileSize);
            if (s.type() == EnemyType.SAMURAI) {
                enemies.add(new Samurai(this, player, ex, s.weapon()));
            } 
            else if(s.type() == EnemyType.NINJA) {
                enemies.add(new Ninja(this, player, ex, s.weapon()));
            }
            else if(s.type() == EnemyType.WARRIORMONK) {
                enemies.add(new WarriorMonk(this, player, ex, s.weapon()));
            }
            else {
                enemies.add(new Ashigaru(this, player, ex, s.weapon()));
            }
        }
        // ศัตรูตัวแรกของทุกด่านดรอปเกราะเสมอ
        if (!enemies.isEmpty()) {
            enemies.get(0).addGuaranteedDrop(ItemType.ARMOUR);
        }

        player.setDefaultPositionOnly();
    }

    public void loadStage(String bgPath) {
        try {
            BufferedImage raw = ImageIO.read(new File(bgPath));

            int scaledW = raw.getWidth() * screenHeight / raw.getHeight();
            background = new BufferedImage(scaledW, screenHeight, BufferedImage.TYPE_INT_ARGB);

            Graphics2D bg = background.createGraphics();
            bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            bg.drawImage(raw, 0, 0, scaledW, screenHeight, null);
            bg.dispose();

            stageWidth = Math.max(scaledW, screenWidth);

            // ด่านที่มืด (เช่น Map2) จะได้ bubble แสง — พิกัดโคมไฟขยายตามสเกลของพื้นหลัง
            lighting = Lighting.forMap(bgPath, (double) screenHeight / raw.getHeight());
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            background = null;
            lighting = null;
            stageWidth = screenWidth;
        }
    }

    /** เริ่มใหม่ด้วยอาวุธที่เลือกไว้ตอนต้น */
    private void resetPlayer() {
        player.setDefault();
        player.swapWeapon(startWeapon);
    }

    void restartGame() {
        resetPlayer();
        loadLevel(levelIndex);
    }

    // ---------------------------------------------------------
    // สลับหน้าจอ (เรียกจาก Screen ต่างๆ)
    // ---------------------------------------------------------
    void showMainMenu() {
        screen = new MainMenuScreen(this);
    }

    void showWeaponSelect() {
        screen = new WeaponSelectScreen(this);
    }

    /** ผู้เล่นเลือกอาวุธแล้ว → เริ่มเกมจากด่าน 1 */
    void startGame(Weapon chosen) {
        startWeapon = chosen;
        screen = null;
        allCleared = false;

        // กันคลิกที่ใช้กดเลือกอาวุธ ไปกลายเป็นการโจมตีทันทีตอนเข้าเกม
        mouseH.leftClicked = false;
        mouseH.blocking = false;
        keyH.ePressed = false;
        keyH.rPressed = false;

        resetPlayer();
        loadLevel(0);
    }

    void quit() {
        gameThread = null;   // ให้ game loop จบ
        System.exit(0);
    }

    /** ฉากหลังของหน้าเมนู: ภาพด่าน 1 เคลือบดำจางๆ */
    void drawBackdrop(Graphics2D g2) {
        if (background != null) g2.drawImage(background, 0, 0, null);
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);
    }

    // ---------------------------------------------------------
    // Game loop (fixed timestep 60 FPS)
    // ---------------------------------------------------------
    @Override
    public void run() {
        double interval = 1_000_000_000.0 / FPS;
        double delta = 0;
        long last = System.nanoTime();

        while (gameThread != null) {
            long now = System.nanoTime();
            delta += (now - last) / interval;
            last = now;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ---------------------------------------------------------
    // Update
    // ---------------------------------------------------------
    public void update() {
        effects.removeIf(Effect::isFinished);

        if (screen != null) {          // อยู่ในเมนู: อัปเดตแค่หน้าเมนู เกมยังไม่เริ่ม
            screen.update();
            return;
        }

        if (!player.isAlive()) {
            if (keyH.rPressed) {
                keyH.rPressed = false;
                restartGame();
            }
            return;
        }
        keyH.rPressed = false;

        player.update();
        for (Enemy e : enemies) e.update();

        updateProjectiles();
        handleEnemyDeaths();
        handlePickup();
        handleItemPickup();
        checkLevelEnd();

        camera.update(player);
    }

    private void updateProjectiles() {
        for (Projectile p : projectiles) {
            p.update();

            if (p.fromPlayer) {
                for (Enemy e : enemies) {
                    if (e.isAlive() && p.getBounds().intersects(e.getBounds())) {
                        e.takeDamage(p.damage);
                        projectiles.remove(p);
                        break;
                    }
                }
            } else if (p.getBounds().intersects(player.getBounds())) {
                if (player.tryParry(p.x)) {
                    effects.add(new ParrySpark(p.x, p.y));
                } else {
                    player.takeDamage(p.damage, p.x);
                }
                projectiles.remove(p);
                continue;
            }

            if (p.isExpired(stageWidth, groundY)) projectiles.remove(p);
        }
    }

    private void handleEnemyDeaths() {
        for (Enemy e : enemies) {
            if (!e.isAlive()) {
                // อาวุธที่ถืออยู่ดรอปเสมอ
                drops.add(new WeaponDrop(e.getX(), groundY - GameConfig.DROP_H, e.getWeapon()));

                // ไอเทม: ศัตรูที่มีของดรอปแน่นอน (เกราะตัวแรกของด่าน, ยาเลือดจากพระ) ดรอปทั้งหมด ไม่งั้นสุ่มตามเรต
                List<ItemType> loot = new ArrayList<>(e.getGuaranteedDrops());
                if (loot.isEmpty()) {
                    ItemType rolled = ItemType.roll();
                    if (rolled != null) loot.add(rolled);
                }
                // วางเรียงต่อจากอาวุธ ไม่ให้ซ้อนกัน
                for (int i = 0; i < loot.size(); i++) {
                    int ix = Math.min(e.getX() + GameConfig.DROP_W + i * GameConfig.ITEM_W,
                                      stageWidth - GameConfig.ITEM_W);
                    items.add(new ItemDrop(ix, groundY - GameConfig.ITEM_H, loot.get(i)));
                }
                enemies.remove(e);
            }
        }
    }

    private void handlePickup() {
        if (!keyH.ePressed) return;
        keyH.ePressed = false;

        for (WeaponDrop d : drops) {
            if (d.getBounds().intersects(player.getBounds())) {
                Weapon old = player.swapWeapon(d.weapon);
                drops.remove(d);
                // อาวุธเดิมตกลงพื้นแทน
                drops.add(new WeaponDrop(player.getX(), groundY - GameConfig.DROP_H, old));
                break;
            }
        }
    }

    /** ไอเทม: เดินทับแล้วใช้ทันที (ถ้าใช้ไม่ได้ เช่น เลือดเต็ม จะค้างไว้บนพื้น) */
    private void handleItemPickup() {
        for (ItemDrop it : items) {
            if (it.getBounds().intersects(player.getBounds()) && it.type.canUse(player)) {
                it.type.apply(player);
                items.remove(it);
            }
        }
    }

    private void checkLevelEnd() {
        if (!enemies.isEmpty()) return;
        if (player.getX() < stageWidth - tileSize * 2) return;

        if (levelIndex + 1 < levels.size()) {
            loadLevel(levelIndex + 1);
        } else {
            allCleared = true;
        }
    }

    // ---------------------------------------------------------
    // Draw
    // ---------------------------------------------------------
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // ขยายภาพ pixel art แบบไม่เบลอ
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        if (screen != null) {          // อยู่ในเมนู: วาดหน้าเมนูแทนตัวเกม
            screen.draw(g2);
            return;
        }

        if (background != null) {
            g2.drawImage(background, -camera.x, 0, null);
        }

        for (WeaponDrop d : drops) d.draw(g2, camera);
        for (ItemDrop i : items) i.draw(g2, camera);
        for (Enemy e : enemies) e.draw(g2);
        for (Projectile p : projectiles) p.draw(g2, camera);
        player.draw(g2);
        for (Effect fx : effects) fx.draw(g2, camera);
        if (lighting != null) lighting.draw(g2, camera, player);   // ความมืดคลุมโลกเกม แต่ไม่คลุม HUD

        drawHUD(g2);
        // ห้าม g2.dispose() — Swing เป็นเจ้าของ Graphics นี้
    }

    private void drawHUD(Graphics2D g2) {
        // หลอดเลือดผู้เล่น
        g2.setColor(Color.darkGray);
        g2.fillRect(20, 20, 200, 20);
        g2.setColor(Color.red);
        g2.fillRect(20, 20, 200 * player.getHP() / player.getMaxHP(), 20);
        g2.setColor(Color.white);
        g2.drawRect(20, 20, 200, 20);

        // หลอดเกราะ (แสดงเฉพาะตอนใส่อยู่)
        if (player.isArmoured()) {
            g2.setColor(Color.darkGray);
            g2.fillRect(20, 42, 200, 6);
            g2.setColor(new Color(110, 160, 230));
            g2.fillRect(20, 42, 200 * player.getArmourHP() / ItemType.ARMOUR_MAX_HP, 6);
            g2.setColor(Color.white);
            g2.drawRect(20, 42, 200, 6);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.drawString("Weapon: " + player.getWeapon().displayName(), 20, 62);
        g2.drawString("Stage " + (levelIndex + 1) + "/" + levels.size()
                + "   Enemies: " + enemies.size(), 20, 84);

        // สถานะจากไอเทม
        int line = 106;
        if (player.isArmoured()) {
            g2.drawString("Armour " + player.getArmourHP() + "/" + ItemType.ARMOUR_MAX_HP
                    + " (-" + (int) (ItemType.ARMOUR_REDUCTION * 100) + "% damage)", 20, line);
            line += 22;
        }
        long boost = player.damageBoostRemainingMs();
        if (boost > 0) {
            g2.drawString(String.format("Damage x%d: %.1fs", ItemType.DAMAGE_MULTIPLIER, boost / 1000.0), 20, line);
            line += 22;
        }

        // ข้อความบอกใบ้เก็บอาวุธ
        for (WeaponDrop d : drops) {
            if (d.getBounds().intersects(player.getBounds())) {
                g2.drawString("[E] pick up " + d.weapon.displayName(), 20, line);
                break;
            }
        }

        g2.setFont(new Font("Arial", Font.BOLD, 40));
        if (!player.isAlive()) {
            drawCentered(g2, "YOU DIED  -  press R to restart");
        } else if (allCleared) {
            drawCentered(g2, "ALL STAGES CLEAR");
        } else if (enemies.isEmpty()) {
            drawCentered(g2, "Stage clear - go right >>>");
        }
    }

    private void drawCentered(Graphics2D g2, String text) {
        int w = g2.getFontMetrics().stringWidth(text);
        g2.setColor(Color.white);
        g2.drawString(text, (screenWidth - w) / 2, screenHeight / 3);
    }
}