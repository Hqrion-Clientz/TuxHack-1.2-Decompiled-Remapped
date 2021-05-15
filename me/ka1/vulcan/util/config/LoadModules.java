// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.util.config;

import java.util.Iterator;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.Vulcan;
import me.ka1.vulcan.module.ModuleManager;
import me.ka1.vulcan.module.Module;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.File;

public class LoadModules
{
    public LoadModules() {
        this.loadHud();
        this.loadCombat();
        this.loadPlayer();
        this.loadClient();
        this.loadMisc();
        this.loadMovement();
        this.loadRender();
    }
    
    public void loadCombat() {
        try {
            final File file = new File(SaveConfiguration.Combat.getAbsolutePath(), "Value.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Combat)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndModConfig(configname, mm);
                        if (mod instanceof Setting.Integer) {
                            ((Setting.Integer)mod).setValue(Integer.parseInt(isOn));
                        }
                        else {
                            if (!(mod instanceof Setting.Double)) {
                                continue;
                            }
                            ((Setting.Double)mod).setValue(Double.parseDouble(isOn));
                        }
                    }
                }
            }
            br.close();
        }
        catch (Exception var13) {
            var13.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Combat.getAbsolutePath(), "Boolean.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Combat)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Boolean)mod).setValue(Boolean.parseBoolean(isOn));
                    }
                }
            }
            br.close();
        }
        catch (Exception var14) {
            var14.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Combat.getAbsolutePath(), "String.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Combat)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Mode)mod).setValue(isOn);
                    }
                }
            }
            br.close();
        }
        catch (Exception var15) {
            var15.printStackTrace();
        }
    }
    
    public void loadPlayer() {
        try {
            final File file = new File(SaveConfiguration.Player.getAbsolutePath(), "Value.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Player)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndModConfig(configname, mm);
                        if (mod instanceof Setting.Integer) {
                            ((Setting.Integer)mod).setValue(Integer.parseInt(isOn));
                        }
                        else {
                            if (!(mod instanceof Setting.Double)) {
                                continue;
                            }
                            ((Setting.Double)mod).setValue(Double.parseDouble(isOn));
                        }
                    }
                }
            }
            br.close();
        }
        catch (Exception var13) {
            var13.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Player.getAbsolutePath(), "Boolean.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Player)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Boolean)mod).setValue(Boolean.parseBoolean(isOn));
                    }
                }
            }
            br.close();
        }
        catch (Exception var14) {
            var14.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Player.getAbsolutePath(), "String.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Player)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Mode)mod).setValue(isOn);
                    }
                }
            }
            br.close();
        }
        catch (Exception var15) {
            var15.printStackTrace();
        }
    }
    
    public void loadClient() {
        try {
            final File file = new File(SaveConfiguration.Client.getAbsolutePath(), "Value.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Client)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndModConfig(configname, mm);
                        if (mod instanceof Setting.Integer) {
                            ((Setting.Integer)mod).setValue(Integer.parseInt(isOn));
                        }
                        else {
                            if (!(mod instanceof Setting.Double)) {
                                continue;
                            }
                            ((Setting.Double)mod).setValue(Double.parseDouble(isOn));
                        }
                    }
                }
            }
            br.close();
        }
        catch (Exception var13) {
            var13.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Client.getAbsolutePath(), "Boolean.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Client)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Boolean)mod).setValue(Boolean.parseBoolean(isOn));
                    }
                }
            }
            br.close();
        }
        catch (Exception var14) {
            var14.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Client.getAbsolutePath(), "String.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Client)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Mode)mod).setValue(isOn);
                    }
                }
            }
            br.close();
        }
        catch (Exception var15) {
            var15.printStackTrace();
        }
    }
    
    public void loadMisc() {
        try {
            final File file = new File(SaveConfiguration.Misc.getAbsolutePath(), "Value.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Misc)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndModConfig(configname, mm);
                        if (mod instanceof Setting.Integer) {
                            ((Setting.Integer)mod).setValue(Integer.parseInt(isOn));
                        }
                        else {
                            if (!(mod instanceof Setting.Double)) {
                                continue;
                            }
                            ((Setting.Double)mod).setValue(Double.parseDouble(isOn));
                        }
                    }
                }
            }
            br.close();
        }
        catch (Exception var13) {
            var13.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Misc.getAbsolutePath(), "Boolean.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Misc)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Boolean)mod).setValue(Boolean.parseBoolean(isOn));
                    }
                }
            }
            br.close();
        }
        catch (Exception var14) {
            var14.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Misc.getAbsolutePath(), "String.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Misc)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Mode)mod).setValue(isOn);
                    }
                }
            }
            br.close();
        }
        catch (Exception var15) {
            var15.printStackTrace();
        }
    }
    
    public void loadMovement() {
        try {
            final File file = new File(SaveConfiguration.Movement.getAbsolutePath(), "Value.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Movement)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndModConfig(configname, mm);
                        if (mod instanceof Setting.Integer) {
                            ((Setting.Integer)mod).setValue(Integer.parseInt(isOn));
                        }
                        else {
                            if (!(mod instanceof Setting.Double)) {
                                continue;
                            }
                            ((Setting.Double)mod).setValue(Double.parseDouble(isOn));
                        }
                    }
                }
            }
            br.close();
        }
        catch (Exception var13) {
            var13.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Movement.getAbsolutePath(), "Boolean.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Movement)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Boolean)mod).setValue(Boolean.parseBoolean(isOn));
                    }
                }
            }
            br.close();
        }
        catch (Exception var14) {
            var14.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Movement.getAbsolutePath(), "String.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Movement)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Mode)mod).setValue(isOn);
                    }
                }
            }
            br.close();
        }
        catch (Exception var15) {
            var15.printStackTrace();
        }
    }
    
    public void loadRender() {
        try {
            final File file = new File(SaveConfiguration.Render.getAbsolutePath(), "Value.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Render)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndModConfig(configname, mm);
                        if (mod instanceof Setting.Integer) {
                            ((Setting.Integer)mod).setValue(Integer.parseInt(isOn));
                        }
                        else {
                            if (!(mod instanceof Setting.Double)) {
                                continue;
                            }
                            ((Setting.Double)mod).setValue(Double.parseDouble(isOn));
                        }
                    }
                }
            }
            br.close();
        }
        catch (Exception var13) {
            var13.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Render.getAbsolutePath(), "Boolean.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Render)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Boolean)mod).setValue(Boolean.parseBoolean(isOn));
                    }
                }
            }
            br.close();
        }
        catch (Exception var14) {
            var14.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Render.getAbsolutePath(), "String.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Render)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Mode)mod).setValue(isOn);
                    }
                }
            }
            br.close();
        }
        catch (Exception var15) {
            var15.printStackTrace();
        }
    }
    
    public void loadHud() {
        try {
            final File file = new File(SaveConfiguration.Hud.getAbsolutePath(), "Value.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Hud)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndModConfig(configname, mm);
                        if (mod instanceof Setting.Integer) {
                            ((Setting.Integer)mod).setValue(Integer.parseInt(isOn));
                        }
                        else {
                            if (!(mod instanceof Setting.Double)) {
                                continue;
                            }
                            ((Setting.Double)mod).setValue(Double.parseDouble(isOn));
                        }
                    }
                }
            }
            br.close();
        }
        catch (Exception var13) {
            var13.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Hud.getAbsolutePath(), "Boolean.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Hud)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Boolean)mod).setValue(Boolean.parseBoolean(isOn));
                    }
                }
            }
            br.close();
        }
        catch (Exception var14) {
            var14.printStackTrace();
        }
        try {
            final File file = new File(SaveConfiguration.Hud.getAbsolutePath(), "String.json");
            final FileInputStream fstream = new FileInputStream(file.getAbsolutePath());
            final DataInputStream in = new DataInputStream(fstream);
            final BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                final String curLine = line.trim();
                final String configname = curLine.split(":")[0];
                final String isOn = curLine.split(":")[1];
                final String m = curLine.split(":")[2];
                for (final Module mm : ModuleManager.getModulesInCategory(Module.Category.Hud)) {
                    if (mm != null && mm.getName().equalsIgnoreCase(m)) {
                        final Setting mod = Vulcan.getInstance().settingsManager.getSettingByNameAndMod(configname, mm);
                        ((Setting.Mode)mod).setValue(isOn);
                    }
                }
            }
            br.close();
        }
        catch (Exception var15) {
            var15.printStackTrace();
        }
    }
}
