//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.render;

import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class FpsLimiter extends Module
{
    Setting.Integer maxFps;
    
    public FpsLimiter() {
        super("FPSLimiter", "fpsLimiter", Category.Client);
    }
    
    @Override
    public void setup() {
        this.maxFps = this.registerInteger("Max Fps", "fpsLimiterMax", 240, 5, 1000);
    }
    
    @Override
    public int onUpdate() {
        FpsLimiter.mc.gameSettings.limitFramerate = this.maxFps.getValue();
        return 0;
    }
}
