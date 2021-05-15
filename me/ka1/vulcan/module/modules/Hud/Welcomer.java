//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.Hud;

import java.awt.Color;
import me.ka1.vulcan.Vulcan;
import java.util.Date;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class Welcomer extends Module
{
    Setting.Integer r;
    Setting.Integer g;
    Setting.Integer b;
    Setting.Integer x;
    Setting.Integer y;
    
    public Welcomer() {
        super("Welcomer", "Displays a welcome message", Category.Hud);
    }
    
    @Override
    public void setup() {
        this.r = this.registerInteger("Red", "red", 255, 0, 255);
        this.g = this.registerInteger("Green", "green", 255, 0, 255);
        this.b = this.registerInteger("Blue", "blue", 255, 0, 255);
        this.x = this.registerInteger("X", "x", 700, 0, 1280);
        this.y = this.registerInteger("Y", "y", 300, 0, 960);
    }
    
    @Override
    public void onRender() {
        if (Welcomer.mc.player.isDead || Welcomer.mc.player == null || Welcomer.mc.world == null) {
            return;
        }
        final Date dt = new Date();
        final int hours = dt.getHours();
        final String name = Welcomer.mc.player.getName();
        if (hours >= 0 && hours < 12) {
            Vulcan.fontRenderer.drawStringWithShadow("Good morning " + name, this.x.getValue(), this.y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
        }
        else if (hours >= 12 && hours <= 16) {
            Vulcan.fontRenderer.drawStringWithShadow("Good afternoon " + name, this.x.getValue(), this.y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
        }
        else if (hours >= 16 && hours <= 21) {
            Vulcan.fontRenderer.drawStringWithShadow("Good evening " + name, this.x.getValue(), this.y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
        }
        else {
            if (hours < 21 || hours > 24) {
                return;
            }
            Vulcan.fontRenderer.drawStringWithShadow("Good night " + name, this.x.getValue(), this.y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
        }
    }
}
