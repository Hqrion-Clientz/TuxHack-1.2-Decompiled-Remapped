//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.render;

import me.ka1.vulcan.module.ModuleManager;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class FOVSlider extends Module
{
    Setting.Double fov;
    
    public FOVSlider() {
        super("FOVSlider", "Lets you change your FOV", Category.Render);
    }
    
    @Override
    public void setup() {
        this.fov = this.registerDouble("FOV", "fov", 115.0, 25.0, 180.0);
    }
    
    @Override
    public void onRender() {
        if (!ModuleManager.isModuleEnabled("Zoom") && FOVSlider.mc.gameSettings.fovSetting != (float)this.fov.getValue()) {
            FOVSlider.mc.gameSettings.fovSetting = (float)this.fov.getValue();
        }
    }
}
