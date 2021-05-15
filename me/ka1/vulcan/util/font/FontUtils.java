//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.util.font;

import me.ka1.vulcan.Vulcan;
import net.minecraft.client.Minecraft;

public class FontUtils
{
    private static final Minecraft mc;
    
    public static float drawStringWithShadow(final boolean customFont, final String text, final int x, final int y, final int color) {
        if (customFont) {
            return Vulcan.fontRenderer.drawStringWithShadow(text, x, y, color);
        }
        return (float)FontUtils.mc.fontRenderer.drawStringWithShadow(text, (float)x, (float)y, color);
    }
    
    public static int getStringWidth(final boolean customFont, final String str) {
        if (customFont) {
            return Vulcan.fontRenderer.getStringWidth(str);
        }
        return FontUtils.mc.fontRenderer.getStringWidth(str);
    }
    
    public static int getFontHeight(final boolean customFont) {
        if (customFont) {
            return Vulcan.fontRenderer.getHeight();
        }
        return FontUtils.mc.fontRenderer.FONT_HEIGHT;
    }
    
    public static float drawKeyStringWithShadow(final boolean customFont, final String text, final int x, final int y, final int color) {
        if (customFont) {
            return Vulcan.fontRenderer.drawStringWithShadow(text, x, y, color);
        }
        return (float)FontUtils.mc.fontRenderer.drawStringWithShadow(text, (float)x, (float)y, color);
    }
    
    static {
        mc = Minecraft.getMinecraft();
    }
}
