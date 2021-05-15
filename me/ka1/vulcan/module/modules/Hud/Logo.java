// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.module.modules.Hud;

import me.ka1.vulcan.setting.Setting;
import net.minecraft.util.ResourceLocation;
import me.ka1.vulcan.module.Module;

public class Logo extends Module
{
    private ResourceLocation logo;
    private static Setting.Integer x;
    private static Setting.Integer y;
    private static Setting.Integer height;
    private static Setting.Integer width;
    private static Setting.Integer uWidth;
    private static Setting.Integer uHeight;
    
    @Override
    public void setup() {
        Logo.x = this.registerInteger("X", "xLogoPos", 0, 0, 1280);
        Logo.y = this.registerInteger("Y", "yLogoPos", 0, 0, 960);
        Logo.height = this.registerInteger("Logo Width", "logoWidth", 64, 32, 2560);
        Logo.width = this.registerInteger("Logo Height", "logoHeight", 64, 32, 2560);
        Logo.uWidth = this.registerInteger("uWidth", "uWidth", 64, 32, 2560);
        Logo.uHeight = this.registerInteger("uWidth", "uWidth", 64, 32, 2560);
    }
    
    public Logo() {
        super("Logo", "Displays a Logo on screen!", Category.Hud);
    }
}
