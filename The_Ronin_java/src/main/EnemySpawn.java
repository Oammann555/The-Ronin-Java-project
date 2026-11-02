package main;

import entity.Weapon;

// record ต้องใช้ Java 16+
public record EnemySpawn(int x, Weapon weapon , EnemyType type) { }
