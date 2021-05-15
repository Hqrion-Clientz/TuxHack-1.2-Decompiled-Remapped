//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.Hud;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import me.ka1.vulcan.Vulcan;
import java.util.List;
import java.util.ArrayList;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class Watermark extends Module
{
    Setting.Integer r;
    Setting.Integer g;
    Setting.Integer b;
    Setting.Integer x;
    Setting.Integer y;
    Setting.Mode mode;
    Setting.Boolean onion;
    
    public Watermark() {
        super("Watermark", "displays the modname and ver (optional)", Category.Hud);
    }
    
    @Override
    public void setup() {
        final ArrayList<String> Modes = new ArrayList<String>();
        Modes.add("TuxHack");
        Modes.add("TuxHack 1");
        Modes.add("OnionWare");
        this.mode = this.registerMode("Mode", "watermarkMode", Modes, "TuxHack");
        this.r = this.registerInteger("Red", "red", 255, 0, 255);
        this.g = this.registerInteger("Green", "green", 255, 0, 255);
        this.b = this.registerInteger("Blue", "blue", 255, 0, 255);
        this.x = this.registerInteger("X", "xWaternark", 1, 0, 1280);
        this.y = this.registerInteger("Y", "yWatermark", 1, 0, 960);
        this.onion = this.registerBoolean("OnionMode", "onion", false);
    }
    
    @Override
    public void onRender() {
        if (this.onion.getValue()) {
            Vulcan.fontRenderer.drawStringWithShadow("TuxHack | " + Minecraft.getDebugFPS() + " | " + Watermark.mc.player.getName(), this.x.getValue(), this.y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
        }
        else {
            Vulcan.fontRenderer.drawStringWithShadow(this.mode.getValue(), this.x.getValue(), this.y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
        }
    }
}
