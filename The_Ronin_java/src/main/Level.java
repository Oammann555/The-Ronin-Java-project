package main;

import entity.Weapon;
import java.util.List;

public class Level {
    public final String bgPath;
    public final List<EnemySpawn> spawns;

    public Level(String bgPath, List<EnemySpawn> spawns) {
        this.bgPath = bgPath;
        this.spawns = spawns;
    }

    public static List<Level> createAll() {
        return List.of(
            new Level("res/Map1.png", List.of(
                new EnemySpawn(600,  Weapon.NINJAKATANA,EnemyType.WARRIORMONK),
                new EnemySpawn(900,  Weapon.YUMI,EnemyType.ASHIGARU),
                new EnemySpawn(1200, Weapon.YARI,EnemyType.SAMURAI)
            )),
            new Level("res/Map1.png", List.of(  
                new EnemySpawn(500,  Weapon.KATANA,EnemyType.ASHIGARU),
                new EnemySpawn(800,  Weapon.YARI,EnemyType.ASHIGARU),
                new EnemySpawn(1000, Weapon.NAGINATA,EnemyType.WARRIORMONK),
                new EnemySpawn(1200, Weapon.KATANA,EnemyType.SAMURAI)
            )),
            new Level("res/Map2.png", List.of(  
                new EnemySpawn(500,  Weapon.NINJAKATANA,EnemyType.NINJA),
                new EnemySpawn(1000,  Weapon.NINJAKATANA,EnemyType.NINJA),
                new EnemySpawn(1000,  Weapon.NINJAKATANA,EnemyType.NINJA),
                new EnemySpawn(1500, Weapon.YUMI,EnemyType.NINJA),
                new EnemySpawn(2050, Weapon.YUMI,EnemyType.NINJA),
                new EnemySpawn(2450, Weapon.NINJAKATANA,EnemyType.NINJA)
            ))
        );
    }
}
