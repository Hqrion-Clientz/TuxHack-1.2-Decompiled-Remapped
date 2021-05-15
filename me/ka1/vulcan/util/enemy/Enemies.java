// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.util.enemy;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

public class Enemies
{
    public static List<Enemy> enemies;
    
    public Enemies() {
        Enemies.enemies = new ArrayList<Enemy>();
    }
    
    public static List<Enemy> getEnemies() {
        return Enemies.enemies;
    }
    
    public static boolean isEnemy(final String name) {
        boolean b = false;
        for (final Enemy e : getEnemies()) {
            if (e.getName().equalsIgnoreCase(name)) {
                b = true;
            }
        }
        return b;
    }
    
    public static Enemy getEnemyByName(final String name) {
        Enemy en = null;
        for (final Enemy e : getEnemies()) {
            if (e.getName().equalsIgnoreCase(name)) {
                en = e;
            }
        }
        return en;
    }
    
    public static void addEnemy(final String name) {
        Enemies.enemies.add(new Enemy(name));
    }
    
    public static void delEnemy(final String name) {
        Enemies.enemies.remove(getEnemyByName(name));
    }
}
