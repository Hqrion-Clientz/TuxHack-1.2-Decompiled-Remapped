//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.Hud;

import java.awt.Color;
import me.ka1.vulcan.Vulcan;
import net.minecraft.client.Minecraft;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class FPS extends Module
{
    Setting.Integer r;
    Setting.Integer g;
    Setting.Integer b;
    Setting.Integer x;
    Setting.Integer y;
    int var1;
    
    public FPS() {
        super("FPS", "fps", Category.Hud);
    }
    
    @Override
    public void setup() {
        this.r = this.registerInteger("Red", "redFps", 255, 0, 255);
        this.g = this.registerInteger("Green", "greenFps", 255, 0, 255);
        this.b = this.registerInteger("Blue", "blueFps", 255, 0, 255);
        this.x = this.registerInteger("X", "xFps", 0, 0, 1280);
        this.y = this.registerInteger("Y", "yFps", 20, 0, 960);
    }
    
    @Override
    public void onRender() {
        this.var1 = Minecraft.getDebugFPS();
        final String toPrint = "FPS: " + this.var1;
        Vulcan.fontRenderer.drawStringWithShadow(toPrint, this.x.getValue(), this.y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
    }
}
