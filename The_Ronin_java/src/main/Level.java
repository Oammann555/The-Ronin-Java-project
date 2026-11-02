package main;

import entity.Weapon;
import java.util.List;

public class Level {
    public final String bgPath;
    public final String introText;
    public final List<EnemySpawn> spawns;

    public Level(String bgPath, String introText, List<EnemySpawn> spawns) {
        this.bgPath = bgPath;
        this.introText = introText;
        this.spawns = spawns;
    }


    public static List<Level> createAll() {
        return List.of(
            new Level("res/Map1.png","The year is 1545. This is the Sengoku Jidai—the Age of Warring States. For over a hundred years, the fires of war have consumed this land."+
            "The Daimyo are consumed by greed, the Shogun is weak, and the people suffer"+
            "\n"+"Right before you stand the Ashigaru: commoners and farmers conscripted into war. They are weak, but cheap and easy to recruit.",
            
             List.of(
                new EnemySpawn(600,  Weapon.NINJAKATANA,EnemyType.ASHIGARU),
                new EnemySpawn(900,  Weapon.YUMI,EnemyType.ASHIGARU),
                new EnemySpawn(1200, Weapon.YARI,EnemyType.SAMURAI)
            )),
            new Level("res/Map1.png","In this age of chaos and famine, a new faction of warriors has emerged: the Ikko-Ikki warrior monks."+
            "Adherents of the Jodo Shinshu sect, they have put down their holy scriptures and picked up weapons."+
            "Once men of peace, they are now a formidable enemy. Exercise extreme caution.", 
            List.of(  
                new EnemySpawn(500,  Weapon.KATANA,EnemyType.WARRIORMONK),
                new EnemySpawn(800,  Weapon.YUMI,EnemyType.ASHIGARU),
                new EnemySpawn(1000, Weapon.NAGINATA,EnemyType.WARRIORMONK),
                new EnemySpawn(1200, Weapon.KATANA,EnemyType.SAMURAI)
            )),
            new Level("res/Map2.png","In this era, intelligence is everything. Ninjas serve as elite intelligence units for the Daimyo in battle, with the Iga and Hattori clans among the most renowned."
            +"They are swift and exceedingly cunning.\n"+"(In reality, ninjas did not dress like their popular stereotypes; instead, they blended seamlessly into the populace as everyday commoners.)", 
            List.of(  
                new EnemySpawn(500,  Weapon.NINJAKATANA,EnemyType.NINJA),
                new EnemySpawn(1000,  Weapon.NINJAKATANA,EnemyType.NINJA),
                new EnemySpawn(1500,  Weapon.NINJAKATANA,EnemyType.NINJA),
                new EnemySpawn(1780, Weapon.YUMI,EnemyType.NINJA),
                new EnemySpawn(2050, Weapon.YUMI,EnemyType.NINJA),
                new EnemySpawn(2450, Weapon.NINJAKATANA,EnemyType.NINJA)
            ))
        );
    }
}
