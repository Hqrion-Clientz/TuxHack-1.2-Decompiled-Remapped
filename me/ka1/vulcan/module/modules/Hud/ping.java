//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.Hud;

import java.awt.Color;
import me.ka1.vulcan.Vulcan;
import java.util.Objects;
import net.minecraft.client.network.NetworkPlayerInfo;
import me.ka1.vulcan.setting.Setting;
import me.ka1.vulcan.module.Module;

public class ping extends Module
{
    Setting.Integer r;
    Setting.Integer g;
    Setting.Integer b;
    Setting.Integer x;
    Setting.Integer y;
    
    public ping() {
        super("Ping", "ping", Category.Hud);
    }
    
    @Override
    public void setup() {
        this.r = this.registerInteger("Red", "redPing", 255, 0, 255);
        this.g = this.registerInteger("Green", "greenPing", 255, 0, 255);
        this.b = this.registerInteger("Blue", "bluePing", 255, 0, 255);
        this.x = this.registerInteger("X", "xPing", 0, 0, 1280);
        this.y = this.registerInteger("Y", "yPing", 30, 0, 960);
    }
    
    public int getPing() {
        int p = -1;
        if (ping.mc.player != null && ping.mc.getConnection() != null) {
            if (ping.mc.getConnection().getPlayerInfo(ping.mc.player.getName()) != null) {
                p = Objects.requireNonNull(ping.mc.getConnection().getPlayerInfo(ping.mc.player.getName())).getResponseTime();
            }
        }
        return p;
    }
    
    @Override
    public void onRender() {
        Vulcan.fontRenderer.drawStringWithShadow(this.getPing() + "ms", this.x.getValue(), this.y.getValue(), new Color(this.r.getValue(), this.g.getValue(), this.b.getValue(), 255).getRGB());
    }
}
