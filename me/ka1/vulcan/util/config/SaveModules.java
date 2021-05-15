// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.util.config;

import java.util.Iterator;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;
import me.ka1.vulcan.Vulcan;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;

public class SaveModules
{
    public void saveModules() {
        this.saveHud();
        this.saveCombat();
        this.savePlayer();
        this.saveClient();
        this.saveMisc();
        this.saveMovement();
        this.saveRender();
    }
    
    public void saveCombat() {
        try {
            final File file = new File(SaveConfiguration.Combat.getAbsolutePath(), "Value.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Combat)) {
                if (i.getType() == Setting.Type.DOUBLE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Double)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
                if (i.getType() == Setting.Type.INT) {
                    out.write(i.getConfigName() + ":" + ((Setting.Integer)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex) {}
        try {
            final File file = new File(SaveConfiguration.Combat.getAbsolutePath(), "Boolean.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Combat)) {
                if (i.getType() == Setting.Type.BOOLEAN) {
                    out.write(i.getConfigName() + ":" + ((Setting.Boolean)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex2) {}
        try {
            final File file = new File(SaveConfiguration.Combat.getAbsolutePath(), "String.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Combat)) {
                if (i.getType() == Setting.Type.MODE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Mode)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex3) {}
    }
    
    public void savePlayer() {
        try {
            final File file = new File(SaveConfiguration.Player.getAbsolutePath(), "Value.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Player)) {
                if (i.getType() == Setting.Type.DOUBLE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Double)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
                if (i.getType() == Setting.Type.INT) {
                    out.write(i.getConfigName() + ":" + ((Setting.Integer)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex) {}
        try {
            final File file = new File(SaveConfiguration.Player.getAbsolutePath(), "Boolean.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Player)) {
                if (i.getType() == Setting.Type.BOOLEAN) {
                    out.write(i.getConfigName() + ":" + ((Setting.Boolean)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex2) {}
        try {
            final File file = new File(SaveConfiguration.Player.getAbsolutePath(), "String.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Player)) {
                if (i.getType() == Setting.Type.MODE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Mode)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex3) {}
    }
    
    public void saveClient() {
        try {
            final File file = new File(SaveConfiguration.Client.getAbsolutePath(), "Value.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Client)) {
                if (i.getType() == Setting.Type.DOUBLE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Double)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
                if (i.getType() == Setting.Type.INT) {
                    out.write(i.getConfigName() + ":" + ((Setting.Integer)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex) {}
        try {
            final File file = new File(SaveConfiguration.Client.getAbsolutePath(), "Boolean.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Client)) {
                if (i.getType() == Setting.Type.BOOLEAN) {
                    out.write(i.getConfigName() + ":" + ((Setting.Boolean)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex2) {}
        try {
            final File file = new File(SaveConfiguration.Client.getAbsolutePath(), "String.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Client)) {
                if (i.getType() == Setting.Type.MODE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Mode)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex3) {}
    }
    
    public void saveMisc() {
        try {
            final File file = new File(SaveConfiguration.Misc.getAbsolutePath(), "Value.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Misc)) {
                if (i.getType() == Setting.Type.DOUBLE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Double)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
                if (i.getType() == Setting.Type.INT) {
                    out.write(i.getConfigName() + ":" + ((Setting.Integer)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex) {}
        try {
            final File file = new File(SaveConfiguration.Misc.getAbsolutePath(), "Boolean.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Misc)) {
                if (i.getType() == Setting.Type.BOOLEAN) {
                    out.write(i.getConfigName() + ":" + ((Setting.Boolean)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex2) {}
        try {
            final File file = new File(SaveConfiguration.Misc.getAbsolutePath(), "String.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Misc)) {
                if (i.getType() == Setting.Type.MODE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Mode)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex3) {}
    }
    
    public void saveMovement() {
        try {
            final File file = new File(SaveConfiguration.Movement.getAbsolutePath(), "Value.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Movement)) {
                if (i.getType() == Setting.Type.DOUBLE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Double)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
                if (i.getType() == Setting.Type.INT) {
                    out.write(i.getConfigName() + ":" + ((Setting.Integer)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex) {}
        try {
            final File file = new File(SaveConfiguration.Movement.getAbsolutePath(), "Boolean.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Movement)) {
                if (i.getType() == Setting.Type.BOOLEAN) {
                    out.write(i.getConfigName() + ":" + ((Setting.Boolean)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex2) {}
        try {
            final File file = new File(SaveConfiguration.Movement.getAbsolutePath(), "String.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Movement)) {
                if (i.getType() == Setting.Type.MODE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Mode)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex3) {}
    }
    
    public void saveRender() {
        try {
            final File file = new File(SaveConfiguration.Render.getAbsolutePath(), "Value.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Render)) {
                if (i.getType() == Setting.Type.DOUBLE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Double)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
                if (i.getType() == Setting.Type.INT) {
                    out.write(i.getConfigName() + ":" + ((Setting.Integer)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex) {}
        try {
            final File file = new File(SaveConfiguration.Render.getAbsolutePath(), "Boolean.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Render)) {
                if (i.getType() == Setting.Type.BOOLEAN) {
                    out.write(i.getConfigName() + ":" + ((Setting.Boolean)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex2) {}
        try {
            final File file = new File(SaveConfiguration.Render.getAbsolutePath(), "String.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Render)) {
                if (i.getType() == Setting.Type.MODE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Mode)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex3) {}
    }
    
    public void saveHud() {
        try {
            final File file = new File(SaveConfiguration.Hud.getAbsolutePath(), "Value.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Hud)) {
                if (i.getType() == Setting.Type.DOUBLE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Double)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
                if (i.getType() == Setting.Type.INT) {
                    out.write(i.getConfigName() + ":" + ((Setting.Integer)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex) {}
        try {
            final File file = new File(SaveConfiguration.Hud.getAbsolutePath(), "Boolean.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Hud)) {
                if (i.getType() == Setting.Type.BOOLEAN) {
                    out.write(i.getConfigName() + ":" + ((Setting.Boolean)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex2) {}
        try {
            final File file = new File(SaveConfiguration.Hud.getAbsolutePath(), "String.json");
            final BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (final Setting i : Vulcan.getInstance().settingsManager.getSettingsByCategory(Module.Category.Hud)) {
                if (i.getType() == Setting.Type.MODE) {
                    out.write(i.getConfigName() + ":" + ((Setting.Mode)i).getValue() + ":" + i.getParent().getName() + "\r\n");
                }
            }
            out.close();
        }
        catch (Exception ex3) {}
    }
}
