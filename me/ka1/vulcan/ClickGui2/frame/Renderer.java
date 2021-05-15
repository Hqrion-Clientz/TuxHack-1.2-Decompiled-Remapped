//Deobfuscated with https://github.com/PetoPetko/Minecraft-Deobfuscator3000 using mappings "1.12 stable mappings"!

// 
// Decompiled by Procyon v0.5.36
// 

package me.ka1.vulcan.ClickGui2.frame;

import me.ka1.vulcan.module.modules.client.ClickGuiModule;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.gui.Gui;
import java.awt.Color;

public class Renderer
{
    public static void drawRectStatic(final int leftX, final int leftY, final int rightX, final int rightY, final Color color) {
        Gui.drawRect(leftX, leftY, rightX, rightY, color.getRGB());
    }
    
    public static void RenderBoxOutline(final double thick, final int x1, final int y1, final int x2, final int y2, final Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GL11.glEnable(2848);
        GL11.glColor3f((float)(color.getRed() / 255), (float)(color.getGreen() / 255), (float)(color.getBlue() / 255));
        GL11.glLineWidth((float)thick);
        GL11.glBegin(2);
        GL11.glVertex2i(x1, y1);
        GL11.glVertex2i(x1, y2);
        GL11.glVertex2i(x2, y2);
        GL11.glVertex2i(x2, y1);
        GL11.glEnd();
        GlStateManager.popMatrix();
    }
    
    public static void renderImage(final int x, final int y, final int xStart, final int yStart, final int width, final int height, final float width2, final float height2) {
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GL11.glColor3f(1.0f, 1.0f, 1.0f);
        Gui.drawModalRectWithCustomSizedTexture(x, y, (float)xStart, (float)yStart, width, height, width2, height2);
    }
    
    public static Color getMainColor() {
        return new Color(ClickGuiModule.red.getValue(), ClickGuiModule.green.getValue(), ClickGuiModule.blue.getValue());
    }
    
    public static Color getTransColor(final boolean hovered) {
        final Color transColor = new Color(0, 0, 0, ClickGuiModule.opacity.getValue() - 50);
        if (hovered) {
            return new Color(0, 0, 0, ClickGuiModule.opacity.getValue());
        }
        return transColor;
    }
    
    public static Color getFontColor(final boolean hovered, final int animating) {
        if (hovered && animating == 0) {
            return new Color(255, 255, 255);
        }
        return new Color(255 - animating, 255 - animating, 255 - animating);
    }
}
