// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.util.config;

import me.ka1.vulcan.Vulcan;

public class Stopper extends Thread
{
    @Override
    public void run() {
        saveConfig();
    }
    
    public static void saveConfig() {
        Vulcan.getInstance().saveModules.saveModules();
        SaveConfiguration.saveBinds();
        SaveConfiguration.saveDrawn();
        SaveConfiguration.saveEnabled();
        SaveConfiguration.saveEnemies();
        SaveConfiguration.saveFont();
        SaveConfiguration.saveFriends();
        SaveConfiguration.saveGUI();
        SaveConfiguration.saveMacros();
        SaveConfiguration.saveMessages();
        SaveConfiguration.savePrefix();
    }
}
